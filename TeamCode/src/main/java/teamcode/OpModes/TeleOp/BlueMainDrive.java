package teamcode.OpModes.TeleOp;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;


@TeleOp
public class BlueMainDrive extends LinearOpMode {

    PoseStorage poseStorage = new PoseStorage();

    Follower driveFollower;

    private double drivePosX;
    private double drivePosY;
    private double driveHeading;
    private Pose driveStartPose;

    private DriveProcesses driveProcesses;
    private Robot robot;



    @Override
    public void runOpMode(){

        robot = new Robot(hardwareMap);

        // Pass the robot instance to DriveProcesses to share the same hardware state
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);


        drivePosX = poseStorage.getPosX();
        drivePosY = poseStorage.getPosY();
        driveHeading = poseStorage.getRobotHeading();

        driveStartPose = new Pose(drivePosX, drivePosY, driveHeading);


        //Create Follower
        driveFollower = Constants.create(hardwareMap);
        //check if I can create the pos here and if it starts with
        driveFollower.setPose(driveStartPose);


        double vertical;
        double horizontal;
        double strafe;





        robot.setAlliance(Robot.Alliance.BLUE);

        waitForStart();
        while(opModeIsActive()){
            vertical = gamepad1.left_stick_y;
            horizontal = gamepad1.right_stick_x;
            strafe = gamepad1.left_stick_x;



            driveFollower.update();
            poseStorage.savePoseToStorage(driveFollower.pose().x(), driveFollower.pose().y(), driveFollower.pose().heading());


            //may not be able to call robot; double check this
            robot.turret.updateTurretTarget(robot);



            //update PID
            robot.shooter.calculateShooterPID();
            //robot.turret.calculateTurretPIDPower();
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
