package teamcode.CustomTuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.Constants.GameConstants;
import teamcode.pedro.Constants;

@Config
@TeleOp
public class TurretTuning extends LinearOpMode {

    //tune kP (until it targets it but oscillates) then kD until it is more consistent and accurate

    public static double kP = 0;
    public static double kD = 0;
    public static double turretHeadingOffsetTolerance = 1.5;
    public static double MIN_POWER = -0.4;
    public static double MAX_POWER = 0.4;
    public static int turretLimit = 110;
    public static double limitPushBack = 0.1;

    double TICKS_PER_REV = 384.5;


    double pinionGearTeeth = 45;
    double turntableGearTeeth = 155;

    double gearRatio = turntableGearTeeth / pinionGearTeeth;

    double currentTurretDegPos;
    double power;

    DcMotorEx pinionMotor;



    private double limitTurretAngTo180Deg(double targAngle){

        while(targAngle > 180){
            targAngle -= 360;
        }
        while(targAngle < -180){
            targAngle += 360;
        }
        return targAngle;
    }

    private void ensureTurretEncoderIsSet(){
        if(pinionMotor.getMode() != DcMotor.RunMode.RUN_WITHOUT_ENCODER){
            pinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }





    public void runOpMode(){
        pinionMotor = hardwareMap.get(DcMotorEx.class, "turretPinion");

        // REVERSE flips power and the encoder together. Positive power still
        // increases the reported heading, but that heading is the other physical way.
        pinionMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        pinionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pinionMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        pinionMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        Follower follower = Constants.create(hardwareMap);

        //ensure startPos is in degrees
        PoseFactory poseFactory = PoseFactory.degrees();

        Pose startPos = null;
        PoseStorage.Alliance startAlliance = null;
        PoseStorage poseStorage = new PoseStorage();

        GameConstants gameConstants = new GameConstants();
        ElapsedTime timer = new ElapsedTime();
        ElapsedTime logTimer = new ElapsedTime();

        //default to center of field if no target is set
        double targetX = 72;
        double targetY = 72;


        while((!isStarted() && !isStopRequested()) && startPos == null){
            if(gamepad1.x){
                //blueGardenStart
                startPos = poseFactory.of(32, 9, 90);
                startAlliance = PoseStorage.Alliance.BLUE;
            } else if (gamepad1.b){
                //blueParkStart
                startPos = poseFactory.of(32, 132, 270);
                startAlliance = PoseStorage.Alliance.BLUE;
            } else if(gamepad1.y){
                //redGardenStart
                startPos = poseFactory.of(110, 133, 270);
                startAlliance = PoseStorage.Alliance.RED;
            } else if(gamepad1.a){
                //redParkStart
                startPos = poseFactory.of(129, 8, 90);
                startAlliance = PoseStorage.Alliance.RED;

            }
            // lets the CPU breathe and process background tasks so it is not overwhelmed by
            // a running while loop
            idle();
        }


        //emergency so it does not crash if no startPos is set; should never need it because
        // the gamepad input will allow me to switch between modes
        if(startPos == null){
            startPos = poseFactory.of(72, 72, 0);
        }

        follower.setPose(startPos);
        if (startAlliance != null) {
            poseStorage.savePoseToStorage(startPos.x(), startPos.y(), startPos.heading());
            poseStorage.setAlliance(startAlliance);
        }

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        double lastError = 0;
        waitForStart();


        while(opModeIsActive()){

            follower.update();
            ensureTurretEncoderIsSet();

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


            double disX = targetX - follower.pose().x();
            double disY = targetY - follower.pose().y();

            double targetFieldBearing = Math.toDegrees(Math.atan2(disY, disX));
            double robotHeadingDeg = Math.toDegrees(follower.pose().heading());
            // Wrap the field bearing into the robot, then clamp to the stops.
            // Do not wrap the turret error. This axis cannot take the short
            // path through the back of the robot.
            double desiredTurretDeg = Range.clip(
                    limitTurretAngTo180Deg(targetFieldBearing - robotHeadingDeg),
                    -turretLimit,
                    turretLimit);

            currentTurretDegPos = (pinionMotor.getCurrentPosition()/(TICKS_PER_REV * gearRatio)) * 360;
            double turretTargAngleError = desiredTurretDeg - currentTurretDegPos;


            double pTerm = turretTargAngleError * kP;

            double dTerm = (deltaTime > 0) ? ((turretTargAngleError - lastError) / deltaTime) * kD : 0;





            if(Math.abs(turretTargAngleError) < turretHeadingOffsetTolerance){
                power = 0;
            } else {
                power = Range.clip(pTerm + dTerm, MIN_POWER, MAX_POWER);
            }
            double powerBeforeLimit = power;
            String limitNote;
            if (currentTurretDegPos > turretLimit && power > 0) {
                power = -limitPushBack;
                limitNote = "past +stop, outward power replaced";
            } else if (currentTurretDegPos < -turretLimit && power < 0) {
                power = limitPushBack;
                limitNote = "past -stop, outward power replaced";
            } else if (currentTurretDegPos > turretLimit || currentTurretDegPos < -turretLimit) {
                limitNote = "outside stop, power already inward";
            } else {
                limitNote = "inside stops";
            }

            // Direction is REVERSE, which flips this command again. The extra
            // minus keeps positive error turning the turret toward the target.
            double motorPower = -power;
            pinionMotor.setPower(motorPower);

            if (logTimer.milliseconds() >= 200) {
                logTimer.reset();
                RobotLog.ii("TurretAim",
                        "state=tuning target=(%.1f, %.1f) field=%.2f robot=%.2f desired=%.2f heading=%.2f ticks=%d error=%.2f p=%.3f d=%.3f powerBefore=%.3f power=%.3f motor=%.3f limit=%s",
                        targetX,
                        targetY,
                        targetFieldBearing,
                        robotHeadingDeg,
                        desiredTurretDeg,
                        currentTurretDegPos,
                        pinionMotor.getCurrentPosition(),
                        turretTargAngleError,
                        pTerm,
                        dTerm,
                        powerBeforeLimit,
                        power,
                        motorPower,
                        limitNote);
            }




            lastError = turretTargAngleError;



            telemetry.addData("robotX: ", follower.pose().x());
            telemetry.addData("robotY: ", follower.pose().y());
            telemetry.addData("targetX", targetX);
            telemetry.addData("targetY", targetY);
            telemetry.addData("currentTurretDegPos", currentTurretDegPos);
            telemetry.addData("targetFieldBearing", targetFieldBearing);
            telemetry.addData("robotHeadingDeg", robotHeadingDeg);
            telemetry.addData("desiredTurretDeg", desiredTurretDeg);
            telemetry.addData("turretTargAngleError", turretTargAngleError);
            telemetry.addData("pTerm", pTerm);
            telemetry.addData("dTerm", dTerm);
            telemetry.addData("powerBeforeLimit", powerBeforeLimit);
            telemetry.addData("power", power);
            telemetry.addData("limit", limitNote);
            telemetry.addData("ticks", pinionMotor.getCurrentPosition());

            telemetry.update();
        }

    }





}
