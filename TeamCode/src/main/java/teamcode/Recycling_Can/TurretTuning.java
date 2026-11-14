package teamcode.Recycling_Can;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import teamcode.Constants.GameConstants;
import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;

@Config
@TeleOp
public class TurretTuning extends LinearOpMode {

    // PID Tuning Guide:
    // 1. Set kD to 0. Increase kP until the turret reaches the target but oscillates.
    // 2. Increase kD until the oscillation stops and the turret settles smoothly.
    public static double kP = 0.002;
    public static double kD = 0;
    public static double turretHeadingOffsetTolerance = 1.5;
    public static double MIN_POWER = -0.25;
    public static double MAX_POWER = 0.25;
    public static int turretLimit = 80;

    private final double TICKS_PER_REV = 384.5;
    //change all of the gear teeth to the updated ones
    private final double pinionGearTeeth = 45.0;
    private final double turntableGearTeeth = 155.0;
    private final double tinyGearTeeth = 1.0;
    private final double axonGearTeeth = 1.0;
    private final double gearRatio = (tinyGearTeeth/axonGearTeeth) * (turntableGearTeeth / pinionGearTeeth);

    private double currentTurretDegPos;
    private double power;
    private double attemptedPower = 0;
    double turretTargAngleError;


    private CRServo turretServo1;
    private CRServo turretServo2;

    private Robot robot;

    public static double targetX;
    public static double targetY;

    private double limitTo180Deg(double targAng){
        double newTargAng = targAng;

        if(Double.isInfinite(newTargAng) || Double.isNaN(newTargAng)) return 0;

        while(newTargAng > 180 || newTargAng < -180){
            if(newTargAng > 180){
                newTargAng -= 360;
            } else {
                newTargAng += 360;
            }
        }

        return newTargAng;
    }

    private void applySoftLimits(){
        // Soft Limits: Allow power that moves AWAY from the limit, block power that moves PAST it.
        // This prevents the high-frequency oscillation (bouncing) at the boundaries.
        if(currentTurretDegPos > turretLimit && power > 0){
            power = 0;
        } else if(currentTurretDegPos < -turretLimit && power < 0){
            power = 0;
        }
    }

    public void runOpMode(){
        //two parallel servo motors

        turretServo1 = hardwareMap.get(CRServo.class, "turretServo1");
        turretServo2 = hardwareMap.get(CRServo.class, "turretServo2");

        //check which needs to be set to reverse, may be turretServo2 instead
        turretServo1.setDirection(CRServo.Direction.REVERSE);

        AnalogInput turretEncoder = hardwareMap.get(AnalogInput.class, "turretEncoder");
        double voltage = turretEncoder.getVoltage();
        double servoZeroPos = (voltage / 3.3) * 360;





        /*
        pinionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Direction is kept as FORWARD per request. 
        // Note: If the turret spins infinitely, the wiring/mounting might require REVERSE.
        pinionMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        pinionMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        //check the direction
        pinionMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        pinionMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

         */

        Follower follower = Constants.create(hardwareMap);
        PoseFactory poseFactory = PoseFactory.degrees();
        Pose startPos = null;
        GameConstants gameConstants = new GameConstants();
        ElapsedTime timer = new ElapsedTime();

        robot = new Robot(hardwareMap);

        targetX = 72;
        targetY = 72;

        // Start position selector
        while((!isStarted() && !isStopRequested()) && startPos == null){
            //add location comments for each Pose (redGarden, etc.)
            if(gamepad1.x) startPos = poseFactory.of(109, 133, 270);
            else if (gamepad1.b) startPos = poseFactory.of(108, 9, 90);
            else if(gamepad1.y) startPos = poseFactory.of(33, 9, 90);
            else if(gamepad1.a) startPos = poseFactory.of(34, 132, 270);
            idle();
        }
        if(startPos == null) startPos = poseFactory.of(72, 72, 0);

        follower.setPose(startPos);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        double lastError = 0;
        waitForStart();

        /*
        if(pinionMotor.getMode() != DcMotor.RunMode.RUN_WITHOUT_ENCODER){
            pinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
        */


        timer.reset();

        while(opModeIsActive()){
            follower.update();


            double currentTurretAngle = ((turretEncoder.getVoltage() / 3.3) * 360) - servoZeroPos;


            // Hardware checks like ensureTurretEncoderIsSet() are removed from the loop for better performance.

            if(gamepad1.dpad_up){
                targetX = gameConstants.redGardenCellPos[0];
                targetY = gameConstants.redGardenCellPos[1];
            } else if(gamepad1.dpad_down){
                targetX = gameConstants.redParkCellPos[0];
                targetY = gameConstants.redParkCellPos[1];
            } else if(gamepad1.dpad_left){
                targetX = gameConstants.blueGardenCellPos[0];
                targetY = gameConstants.blueGardenCellPos[1];
            } else if(gamepad1.dpad_right){
                targetX = gameConstants.blueParkCellPos[0];
                targetY = gameConstants.blueParkCellPos[1];
            }

            double deltaTime = timer.seconds();
            timer.reset();

            if(deltaTime <= 0){
                deltaTime = 0.001;
            }

            double disX = targetX - follower.pose().x();
            double disY = targetY - follower.pose().y();
            
            // 1. Calculate field-centric bearing
            double targetFieldBearing = Math.toDegrees(Math.atan2(disY, disX));
            
            // 2. Convert to robot-relative target
            double offsetFromRobotHeading = Range.clip(limitTo180Deg( targetFieldBearing - Math.toDegrees(follower.pose().heading())), -turretLimit, turretLimit);
            
            // 3. Calculate current turret heading and resulting error
            turretTargAngleError = offsetFromRobotHeading - currentTurretDegPos;



            // 4. PID Calculation
            double pTerm = turretTargAngleError * kP;
            double dTerm = (deltaTime > 0.001) ? ((turretTargAngleError - lastError) / deltaTime) * kD : 0;

            attemptedPower = pTerm + dTerm;
            
            // 5. Apply Deadzone and Clipping
            if(Math.abs(turretTargAngleError) < turretHeadingOffsetTolerance){
                power = 0;
            } else {
                power = Range.clip(attemptedPower, MIN_POWER, MAX_POWER);
            }
            
            // 6. Safety Limits
            applySoftLimits();

            lastError = turretTargAngleError;

            robot.drivetrain.updateDriveMotorPowers(gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);




            // Telemetry for Debugging
            telemetry.addData("Robot Pose", "X: %.1f, Y: %.1f, H: %.1f", 
                follower.pose().x(), follower.pose().y(), Math.toDegrees(follower.pose().heading()));
            telemetry.addData("Target Pos", "X: %.1f, Y: %.1f", targetX, targetY);
            telemetry.addData("Turret Heading", "%.2f deg", currentTurretDegPos);
            telemetry.addData("Error", "%.2f deg", turretTargAngleError);
            telemetry.addData("Power (P+D)", "%.3f", attemptedPower);
            telemetry.addData("Final Power", "%.3f", power);
            telemetry.update();

        }
    }
}
