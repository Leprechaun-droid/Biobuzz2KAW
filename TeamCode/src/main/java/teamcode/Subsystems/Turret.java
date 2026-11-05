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
    double power = 0;

    private double lastError = 0;
    private final ElapsedTime timer = new ElapsedTime();
    DcMotorEx turretPinionMotor;

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

    private void ensureEncoderTracking() {
        if (turretPinionMotor.getMode() != DcMotor.RunMode.RUN_WITHOUT_ENCODER) {
            turretPinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    String targetCell;
    private static volatile double turretTargetX;
    private static volatile double turretTargetY;

    public void updateTurretTarget(){
        PoseStorage poseStorage = new PoseStorage();
        PoseStorage.Alliance alliance = poseStorage.getAlliance();
        double y = poseStorage.getPosY();

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
            turretTargetX = 0;
            turretTargetY = 0;
        }
    }

    public void calculateTurretPIDPower(double posX, double posY, double robotHeading, double turretHeading){
        ensureEncoderTracking();

        PoseStorage.Alliance alliance = new PoseStorage().getAlliance();
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
        double fieldBearing = Math.toDegrees(Math.atan2(disY, disX));

        // 3. Error = Target - RobotOrientation - TurretOrientation
        // normalizeDegrees ensures the turret turns -10 deg instead of +350 deg
        double error = normalizeDegrees(fieldBearing - Math.toDegrees(robotHeading) - turretHeading);

        double pTerm = error * kP;
        
        // The dTerm: (Change in Error / Change in Time) * kD
        // It acts as a brake when we approach the target quickly.
        double dTerm = (deltaTime > 0) ? (error - lastError) / deltaTime * kD : 0;

        double headingOffsetTolerance = 0.8; // Tolerance in degrees
        if(Math.abs(error) < headingOffsetTolerance){
            power = 0;
        } else {
            power = Range.clip(pTerm + dTerm, -0.6, 0.6);
        }

        lastError = error;
        slipRing();
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

    public void slipRing() {
        // goBILDA 435 RPM motor: 384.5 counts per motor-output revolution.
        double ticksPerTurretRev = 384.5 * turretGearRatio;
        double turretDegreePos = (turretPinionMotor.getCurrentPosition() / ticksPerTurretRev) * 360.0;

        double limit = 70;
        
        // Soft Limit Logic:
        // We only cut power if we are past the limit AND trying to turn further in that direction.
        if (turretDegreePos > limit && power > 0) {
            power = 0;
        } else if (turretDegreePos < -limit && power < 0) {
            power = 0;
        }
        
        turretPinionMotor.setPower(power);
    }


    public double getTurretHeading(){
        ensureEncoderTracking();
        // Degrees. 384.5 is the 435 RPM motor count, then the pinion-to-turntable ratio.
        return turretPinionMotor.getCurrentPosition() / (384.5 * turretGearRatio) * 360;
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


    public double getTurretPos(){
        ensureEncoderTracking();
        return turretPinionMotor.getCurrentPosition();
    }
}
