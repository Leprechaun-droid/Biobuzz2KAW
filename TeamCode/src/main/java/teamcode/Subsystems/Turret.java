package teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import teamcode.ComplexMath.Storage.PoseStorage;

public class Turret {

    // PID constants for degrees. 
    // kP is the "strength" of the turn.
    // kD is the "damping" that prevents overshooting.
    private double kP = 0.016;
    private double kD = 0.001;

    private double turretLimit = 110;
    private double limitPushBack = 0.1;

    DcMotorEx turretPinionMotor;
    PoseStorage poseStorage = new PoseStorage();
    PoseStorage.Alliance alliance;

    public Turret(HardwareMap hardwareMap){
        turretPinionMotor = hardwareMap.get(DcMotorEx.class, "turretPinion");

        // Direction has to be set before the encoder reset. REVERSE flips power
        // and the encoder together, so positive heading still matches positive power.
        turretPinionMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        turretPinionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reset encoder to 0 at start. Point turret forward before hitting INIT.
        // STOP_AND_RESET is sticky if RUN_WITHOUT_ENCODER is not applied again,
        // and a stuck reset reports 0 ticks while the motor keeps spinning.
        turretPinionMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretPinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    private boolean isTurretModeSet = false;
    private void ensureTurretEncoderIsSet(){
        //checking a boolean value saves more
        //using this when it is redundant strains the CPu, so it is not used for now
        if(!isTurretModeSet){
            turretPinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            isTurretModeSet = true;
        }
    }

    String targetCell;
    //check why volatile is necessary; I dont see why it is if it can just be updated for each opmode
    private static volatile double turretTargetX;
    private static volatile double turretTargetY;

    public void updateTurretTarget(double y){
        alliance = poseStorage.getAlliance();

        // Target coordinates based on alliance and position
        if(alliance == PoseStorage.Alliance.RED) {
            if(y < 72) {
                targetCell = "RED_GARDEN";
                turretTargetX = 84.79;
                turretTargetY = 87.32;

            } else {
                targetCell = "RED_PARK";
                turretTargetX = 83.84;
                turretTargetY = 50.27;
            }
        } else if(alliance == PoseStorage.Alliance.BLUE) {
            if(y < 72) {
                targetCell = "BLUE_PARK";
                turretTargetX = 58.05;
                turretTargetY = 86.41;

            } else {
                targetCell = "BLUE_GARDEN";
                turretTargetX = 57.82;
                turretTargetY = 53.41;

            }
        } else {
            //set center as default target if no other targets are working
            turretTargetX = 72;
            turretTargetY = 72;
        }
    }

    double power = 0;

    //having lastError set to zero will allow  it to correctly calculate the first loop without error
    private double lastError = 0;
    private double desiredTurretDeg = 0;
    private double turretTargAngleError = 0;
    private String limitNote = "not run";
    private final ElapsedTime timer = new ElapsedTime();
    private final ElapsedTime logTimer = new ElapsedTime();
    public void calculateTurretPIDPower(double posX, double posY, double robotHeading){
        //be cautious of this (calling it after the turret rotates could cause it to have a wrong 0deg reference heading)
        //this could also
        // only add this back if we continue to have issues with the turret turning
        // the constructor should set it fine, and this will either cause CPU strain or errors
        //ensureTurretEncoderIsSet();


        alliance = poseStorage.getAlliance();
        if (alliance == null || alliance == PoseStorage.Alliance.NONE) {
            power = 0;
            lastError = 0;
            limitNote = "alliance unset, power forced to 0";
            turretPinionMotor.setPower(0);
            logAim("idle", 0);
            return;
        }

        double deltaTime = timer.seconds();
        timer.reset();

        // 1. Calculate vector to target
        double disX = turretTargetX - posX;
        double disY = turretTargetY - posY;

        // Field bearing is [-180, 180]. Pedro heading is [0, 360). Wrap that
        // difference first, then clamp it inside the turret stops. The turret
        // cannot spin through the back, so the error stays a plain subtraction.
        // Wrapping it again picks the short path into the stop: a +33° cell
        // became a -172° command while the turret sat at -155°.
        double targetFieldBearing = Math.toDegrees(Math.atan2(disY, disX));
        desiredTurretDeg = Range.clip(
                normalizeDegrees(targetFieldBearing - Math.toDegrees(robotHeading)),
                -turretLimit,
                turretLimit);
        turretTargAngleError = desiredTurretDeg - getTurretHeading();

        double pTerm = turretTargAngleError * kP;
        
        // The dTerm: (Change in Error / Change in Time) * kD
        // It acts as a brake when we approach the target quickly.
        double dTerm = (deltaTime > 0) ? (turretTargAngleError - lastError) / deltaTime * kD : 0;

        //tune offset tolerance
        double headingOffsetTolerance = 1.5; // Tolerance in degrees
        if(Math.abs(turretTargAngleError) < headingOffsetTolerance){
            power = 0;
        } else {
            //add MIN_POWER and MAX_POWER variables so they can be changed with greater ease
            power = Range.clip(pTerm + dTerm, -0.4, 0.4);
        }

        lastError = turretTargAngleError;
        limitTurretRange();

        double motorPower = -power;
        turretPinionMotor.setPower(motorPower);
        logAim("aim", motorPower);
    }

    private void logAim(String state, double motorPower) {
        if (logTimer.milliseconds() < 200) {
            return;
        }
        logTimer.reset();
        RobotLog.ii("TurretAim",
                "state=%s alliance=%s target=(%.1f, %.1f) desired=%.2f heading=%.2f ticks=%d error=%.2f power=%.3f motor=%.3f limit=%s",
                state,
                alliance,
                turretTargetX,
                turretTargetY,
                desiredTurretDeg,
                getTurretHeading(),
                turretPinionMotor.getCurrentPosition(),
                turretTargAngleError,
                power,
                motorPower,
                limitNote);
    }



    private void limitTurretRange(){
        double heading = getTurretHeading();
        if (heading > turretLimit && power > 0) {
            power = -limitPushBack;
            limitNote = "past +stop, outward power replaced";
        } else if (heading < -turretLimit && power < 0) {
            power = limitPushBack;
            limitNote = "past -stop, outward power replaced";
        } else if (heading > turretLimit || heading < -turretLimit) {
            limitNote = "outside stop, power already inward";
        } else {
            limitNote = "inside stops";
        }
    }

    // Shortest path logic: keeps values between -180 and 180
    private double normalizeDegrees(double degrees) {
        while (degrees > 180) degrees -= 360;
        while (degrees < -180) degrees += 360;
        return degrees;
    }

    private final double turntableGearTeeth = 155.0;
    private final double pinionGearTeeth = 45.0;
    final double turretGearRatio = turntableGearTeeth / pinionGearTeeth;



    public double getTurretHeading(){
        //ensureTurretEncoderIsSet();
        // Degrees. 384.5 is the 435 RPM motor count, then the pinion-to-turntable ratio.
        return (turretPinionMotor.getCurrentPosition() / (384.5 * turretGearRatio)) * 360;
    }

    public void setTurretPD(double newkP, double newKD){
        kP = newkP;
        kD = newKD;
    }

    public void resetTimer(){
        timer.reset();
    }



    public double getTurretTargetX(){
        return turretTargetX;
    }

    public double getTurretTargetY(){
        return turretTargetY;
    }


    public double getTurretTickPos(){

        return turretPinionMotor.getCurrentPosition();
    }

    public double getDesiredTurretDeg() {
        return desiredTurretDeg;
    }

    public double getTurretErrorDeg() {
        return turretTargAngleError;
    }

    public double getTurretPower() {
        return power;
    }

    public double getKP() {
        return kP;
    }

    public String getLimitNote() {
        return limitNote;
    }
}
