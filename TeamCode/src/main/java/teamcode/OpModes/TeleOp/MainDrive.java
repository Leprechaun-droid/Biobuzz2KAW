package teamcode.OpModes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;


@TeleOp
public class MainDrive extends LinearOpMode {


    PoseStorage poseStorage = new PoseStorage();
    Follower driveFollower;


    /*
        - CREATE POSITIONS FOR THE CELLS EITHER IN THIS CLASS OR IN ANOTHER ONE
        THAT HANDLES THE MATH
    */


    private double startDrivePosX;
    private double startDrivePosY;
    private double startDriveHeading;
    private Pose driveStartPose;


    private Robot robot;
    private DriveProcesses driveProcesses;

    @Override
    public void runOpMode(){
        robot = new Robot(hardwareMap);

        // Pass the robot instance to DriveProcesses to share the same hardware state
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);


        //Create Follower
            driveFollower = Constants.create(hardwareMap);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        if (poseStorage.isPoseSet()) {
            startDrivePosX = poseStorage.getPosX();
            startDrivePosY = poseStorage.getPosY();
            startDriveHeading = poseStorage.getRobotHeading();
            driveStartPose = new Pose(startDrivePosX, startDrivePosY, startDriveHeading);
        } else {
            // Same starts as TurretTuning. Degrees, with 0 facing +X.
            PoseFactory poseFactory = PoseFactory.degrees();
            driveStartPose = null;
            while ((!isStarted() && !isStopRequested()) && driveStartPose == null) {
                if (gamepad1.x) {
                    driveStartPose = poseFactory.of(32, 9, 90);
                    poseStorage.setAlliance(PoseStorage.Alliance.BLUE);
                } else if (gamepad1.b) {
                    driveStartPose = poseFactory.of(32, 132, 270);
                    poseStorage.setAlliance(PoseStorage.Alliance.BLUE);
                } else if (gamepad1.y) {
                    driveStartPose = poseFactory.of(110, 133, 270);
                    poseStorage.setAlliance(PoseStorage.Alliance.RED);
                } else if (gamepad1.a) {
                    driveStartPose = poseFactory.of(129, 8, 90);
                    poseStorage.setAlliance(PoseStorage.Alliance.RED);
                }
                telemetry.addLine("No saved pose. Same starts as Turret Tuning:");
                telemetry.addLine("X blue garden (32, 9, 90)");
                telemetry.addLine("B blue park (32, 132, 270)");
                telemetry.addLine("Y red garden (110, 133, 270)");
                telemetry.addLine("A red park (129, 8, 90)");
                telemetry.update();
                idle();
            }
            if (driveStartPose == null) {
                driveStartPose = poseFactory.of(72, 72, 0);
            }
            poseStorage.savePoseToStorage(driveStartPose.x(), driveStartPose.y(), driveStartPose.heading());
            // Logged Turret Tuning gain. kD stays the match damping.
            robot.turret.setTurretPD(0.016, 0.001);
        }

        driveFollower.setPose(driveStartPose);


        double vertical;
        double horizontal;
        double strafe;






        //robot.vision.initVisionPortal();
        //robot.vision.initAprilTag();
        //robot.vision.resetScanner();
        if (!isStarted()) {
            waitForStart();
        }
        while(opModeIsActive()){
            vertical = gamepad1.left_stick_y;
            horizontal = gamepad1.right_stick_x;
            strafe = gamepad1.left_stick_x;


            driveFollower.update();
            poseStorage.savePoseToStorage(driveFollower.pose().x(), driveFollower.pose().y(), driveFollower.pose().heading());




            //update PID
            robot.shooter.calculateShooterPID();
            robot.turret.updateTurretTarget(driveFollower.pose().y());
            robot.turret.calculateTurretPIDPower(poseStorage.getPosX(), poseStorage.getPosY(), poseStorage.getRobotHeading());


            //updateSubsystems
            driveProcesses.intakeDriveUpdate();
            driveProcesses.shooterDriveUpdate();
            robot.drivetrain.updateFieldCentricDrive(
                    vertical, strafe, horizontal, driveFollower.pose().heading());









            telemetry.addData("lf_motor_power", robot.drivetrain.getLFMotorPower());
            telemetry.addData("lb_motor_power", robot.drivetrain.getLBMotorPower());
            telemetry.addData("rf_motor_power", robot.drivetrain.getRFMotorPower());
            telemetry.addData("rb_motor_power", robot.drivetrain.getRBMotorPower());
            telemetry.addData("left_toggler_analog", gamepad1.left_trigger);
            telemetry.addData("ShooterRPM", robot.shooter.getRPM());
            telemetry.addData("CurrentCluster", robot.vision.getCurrentCluster());
            telemetry.addData("Target RPM", robot.shooter.getTargetRPM());
            telemetry.addData("Position", "%.1f, %.1f, %.1f deg",
                    poseStorage.getPosX(), poseStorage.getPosY(), Math.toDegrees(poseStorage.getRobotHeading()));
            telemetry.addData("alliance", poseStorage.getAlliance());
            telemetry.addData("targetPosX", robot.turret.getTurretTargetX());
            telemetry.addData("targetPosY", robot.turret.getTurretTargetY());
            telemetry.addData("turret heading", robot.turret.getTurretHeading());
            telemetry.addData("turret ticks", robot.turret.getTurretTickPos());


            //telemetry.addData("shooterPower", shooter.getShooterPower());
            //telemetry.addData("shooterRPM", robot.shooter.getRPM());
            telemetry.update();
            sendFieldDisplay(driveFollower.pose());
            //telemetry.addData("robotPose", "%2f, %2f", follower.pose().x(), follower.pose().y());


        }
    }

    // Dashboard field coordinates are centered on the field. Pedro poses use a
    // corner origin, so both axes shift by half a 144 inch field.
    private void sendFieldDisplay(Pose pose) {
        double robotHeadingDeg = Math.toDegrees(pose.heading());
        double turretHeadingDeg = robot.turret.getTurretHeading();
        TelemetryPacket packet = new TelemetryPacket();
        packet.put("robot heading", robotHeadingDeg);
        packet.put("turret heading", turretHeadingDeg);
        packet.put("turret desired", robot.turret.getDesiredTurretDeg());
        packet.put("turret kP", robot.turret.getKP());
        packet.put("alliance", poseStorage.getAlliance());

        Canvas field = packet.fieldOverlay();
        double x = pose.x() - 72.0;
        double y = pose.y() - 72.0;
        double heading = pose.heading();
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);
        double halfLength = 8.0;
        double halfWidth = 8.0;
        double[] forward = {halfLength, halfLength, -halfLength, -halfLength};
        double[] right = {halfWidth, -halfWidth, -halfWidth, halfWidth};
        double[] xs = new double[4];
        double[] ys = new double[4];
        for (int i = 0; i < 4; i++) {
            xs[i] = x + forward[i] * cos + right[i] * sin;
            ys[i] = y + forward[i] * sin - right[i] * cos;
        }
        field.setStroke("#3F51B5");
        field.strokeLine(xs[0], ys[0], xs[1], ys[1]);
        field.strokeLine(xs[1], ys[1], xs[2], ys[2]);
        field.strokeLine(xs[2], ys[2], xs[3], ys[3]);
        field.strokeLine(xs[3], ys[3], xs[0], ys[0]);

        field.setStroke("#00C853");
        field.strokeLine(x, y, x + cos * 14.0, y + sin * 14.0);

        double turretField = heading + Math.toRadians(turretHeadingDeg);
        field.setStroke("#FF6D00");
        field.strokeLine(x, y,
                x + Math.cos(turretField) * 18.0,
                y + Math.sin(turretField) * 18.0);

        field.setFill("#F44336");
        field.fillCircle(robot.turret.getTurretTargetX() - 72.0, robot.turret.getTurretTargetY() - 72.0, 1.5);
        FtcDashboard.getInstance().sendTelemetryPacket(packet);
    }









}
