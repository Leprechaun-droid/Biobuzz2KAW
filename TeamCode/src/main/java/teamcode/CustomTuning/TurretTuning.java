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
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

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

    private void limitTestTurretRange(){

        if(currentTurretDegPos > turretLimit){
            power = Math.min(power, -limitPushBack);
        } else if(currentTurretDegPos < -turretLimit){
            power = Math.max(power, limitPushBack);
        }

    }

    private void ensureTurretEncoderIsSet(){
        if(pinionMotor.getMode() != DcMotor.RunMode.RUN_WITHOUT_ENCODER){
            pinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }





    public void runOpMode(){
        pinionMotor = hardwareMap.get(DcMotorEx.class, "turretPinion");

        pinionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pinionMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        pinionMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        Follower follower = Constants.create(hardwareMap);

        //ensure startPos is in degrees
        PoseFactory poseFactory = PoseFactory.degrees();

        Pose startPos = null;

        GameConstants gameConstants = new GameConstants();
        ElapsedTime timer = new ElapsedTime();

        //default to center of field if no target is set
        double targetX = 72;
        double targetY = 72;


        while((!isStarted() && !isStopRequested()) && startPos == null){
            if(gamepad1.x){
                //blueGardenStart
                startPos = poseFactory.of(32, 9, 90);
            } else if (gamepad1.b){
                //blueParkStart
                startPos = poseFactory.of(32, 132, 270);
            } else if(gamepad1.y){
                //redGardenStart
                startPos = poseFactory.of(110, 133, 270);
            } else if(gamepad1.a){
                //redParkStart
                startPos = poseFactory.of(129, 8, 90);

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

            double offsetFromRobotHeadingToTargHeading = targetFieldBearing - Math.toDegrees(follower.pose().heading());

            //add limit here (this calculates the degrees from the turret angle to the robotHeadingOffset)
            // look for where to add the 360 degree limit
            currentTurretDegPos = (pinionMotor.getCurrentPosition()/(TICKS_PER_REV * gearRatio)) * 360;
            double turretTargAngleError = limitTurretAngTo180Deg(offsetFromRobotHeadingToTargHeading - currentTurretDegPos);


            double pTerm = turretTargAngleError * kP;

            double dTerm = (deltaTime > 0) ? ((turretTargAngleError - lastError) / deltaTime) * kD : 0;





            if(Math.abs(turretTargAngleError) < turretHeadingOffsetTolerance){
                power = 0;
            } else {
                power = Range.clip(pTerm + dTerm, MIN_POWER, MAX_POWER);
            }
            limitTestTurretRange();

            pinionMotor.setPower(power);




            lastError = turretTargAngleError;



            telemetry.addData("robotX: ", follower.pose().x());
            telemetry.addData("robotY: ", follower.pose().y());
            telemetry.addData("targetX", targetX);
            telemetry.addData("targetY", targetY);
            telemetry.addData("currentTurretDegPos", currentTurretDegPos);
            telemetry.addData("targetFieldBearing", targetFieldBearing);
            telemetry.addData("offsetFromRobotHeadingToTargHeading", offsetFromRobotHeadingToTargHeading);
            telemetry.addData("turretTargAngleError", turretTargAngleError);
            telemetry.addData("pTerm", pTerm);
            telemetry.addData("dTerm", dTerm);
            telemetry.addData("power", power);
            telemetry.addData("currentTurretTickPos", pinionMotor.getCurrentPosition() / gearRatio);

            telemetry.update();
        }

    }





}
