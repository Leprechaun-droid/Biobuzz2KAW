package teamcode.Tests;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;

@Config
@TeleOp
public class MatchLoaderTuning extends LinearOpMode {

    public static double matchLoaderRaisePos = 0.5;
    public static double matchLoaderLowerPos = 0.2;

    Servo matchLoader;
    DriveProcesses driveProcesses;
    Robot robot;

    @Override
    public void runOpMode(){

        matchLoader = hardwareMap.get(Servo.class, "matchLoader");
        robot = new Robot(hardwareMap);
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);
        //likely not needed
        //telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        waitForStart();


        while(opModeIsActive()){

            double vertical = gamepad1.left_stick_y;
            double horizontal = gamepad1.right_stick_x;
            double strafe = gamepad1.left_stick_x;

            if(gamepad1.a){
                matchLoader.setPosition(matchLoaderRaisePos);
            } else if (gamepad1.b){
                matchLoader.setPosition(matchLoaderLowerPos);
            }


            driveProcesses.intakeDriveUpdate();
            robot.drivetrain.updateDriveMotorPowers(vertical, horizontal, strafe);

            telemetry.update();

        }







    }







}
