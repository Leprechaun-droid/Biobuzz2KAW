package teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import teamcode.ComplexMath.Storage.PoseStorage;

public class Turret {

    // PID constants for degrees. 
    // kP is the "strength" of the turn.
    // kD is the "damping" that prevents overshooting.
    private double kP = 0.015;
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
        turretPinionMotor.setDirection(DcMotorSimple.Direction.FORWARD);
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
    private final ElapsedTime timer = new ElapsedTime();
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
            turretPinionMotor.setPower(0);
            return;
        }

        double deltaTime = timer.seconds();
        timer.reset();

        // 1. Calculate vector to target
        double disX = turretTargetX - posX;
        double disY = turretTargetY - posY;

        // 2. Calculate the global field bearing in degrees
        double targetFieldBearing = Math.toDegrees(Math.atan2(disY, disX));

        // 3. Error = Target - RobotOrientation - TurretOrientation
        // normalizeDegrees ensures the turret turns -10 deg instead of +350 deg
        double offsetFromRobotHeadingToTargHeading = targetFieldBearing - Math.toDegrees(robotHeading);

        //check that checkTurretHeading returns an accurate reading
        //change the name to the name of the corresponding method in the TurretTuning file
        double turretTargAngleError = normalizeDegrees(offsetFromRobotHeadingToTargHeading - getTurretHeading());

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

        turretPinionMotor.setPower(power);
    }



    private void limitTurretRange(){

        if(getTurretHeading() > turretLimit){
            power = Math.min(power, -limitPushBack);
        } else if(getTurretHeading() < -turretLimit){
            power = Math.max(power, limitPushBack);
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
}
