package teamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;


@TeleOp
public class MainDrive extends LinearOpMode {




    private Robot robot;
    private DriveProcesses driveProcesses;

    @Override
    public void runOpMode(){
        robot = new Robot(hardwareMap);
        // Pass the robot instance to DriveProcesses to share the same hardware state
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);

        double vertical;
        double horizontal;
        double strafe;

        //robot.vision.initVisionPortal();
        //robot.vision.initAprilTag();
        //robot.vision.resetScanner();
        waitForStart();
        while(opModeIsActive()){
            vertical = gamepad1.left_stick_y;
            horizontal = gamepad1.right_stick_x;
            strafe = gamepad1.left_stick_x;

            //constant processes
            //robot.vision.scanForClusters();


            //update PID
            robot.shooter.calculateShooterPID();
            //robot.turret.calculateTurretPIDPower(robot.vision.getCurrentCluster());
            //robot.turret.slipRing();

            //updateSubsystems
            driveProcesses.intakeDriveUpdate();
            driveProcesses.shooterDriveUpdate();
            robot.drivetrain.updateDriveMotorPowers(vertical, horizontal, strafe);









            telemetry.addData("lf_motor_power", robot.drivetrain.getLFMotorPower());
            telemetry.addData("lb_motor_power", robot.drivetrain.getLBMotorPower());
            telemetry.addData("rf_motor_power", robot.drivetrain.getRFMotorPower());
            telemetry.addData("rb_motor_power", robot.drivetrain.getRBMotorPower());
            telemetry.addData("left_toggler_analog", gamepad1.left_trigger);
            telemetry.addData("ShooterRPM", robot.shooter.getRPM());
            telemetry.addData("CurrentCluster", robot.vision.getCurrentCluster());
            telemetry.addData("Target RPM", robot.shooter.getTargetRPM());


            //telemetry.addData("shooterPower", shooter.getShooterPower());
            //telemetry.addData("shooterRPM", robot.shooter.getRPM());
            telemetry.update();
            //telemetry.addData("robotPose", "%2f, %2f", follower.pose().x(), follower.pose().y());


        }
    }





}
