package teamcode.CustomTuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.Constants.GameConstants;
import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;
import teamcode.Subsystems.Shooter;
import teamcode.pedro.Constants;


@Config
@TeleOp
public class ShooterTuning extends LinearOpMode {
    public static double kV = 0.0008;
    public static double kS = 0.07;
    public static double kP = 0.005;

    //540 is a good number, it is give or take 10
    public static int goalRPM = 2240;

    private Shooter shooter;

    // ------------------------------------
    // Turret
    // ------------------------------------

    private DriveProcesses driveProcesses;

    private Robot robot;
    private Follower follower;

    public void runOpMode(){
        shooter = new Shooter(hardwareMap);
        robot = new Robot(hardwareMap);
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);
        GameConstants gameConstants = new GameConstants();
        Pose startPos = new Pose(10, 133, 270);

        follower = Constants.create(hardwareMap);
        follower.setPose(startPos);


        double targetPosX = gameConstants.blueParkCellPos[0];
        double targetPosY = gameConstants.blueParkCellPos[1];

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        waitForStart();
        while(opModeIsActive()){

            // Quick Tuning Tutorial
            //      1. set kV and kP to 0, then tune kS until the flywheel begins spinning
            //          - this establishes the basic amount of power needed to get the flywheel moving
            //      2. tune kV until the graph is bouncing up and down over the target velocity with relative accuracy
            //          - minimizes the work kP needs to do, allowing for it to be more accurate
            //      3. tune kP until this is narrowed even further


            double dis = Math.sqrt(Math.pow((targetPosX - follower.pose().x()), 2) - Math.pow((targetPosY - follower.pose().y()), 2));

            double feedForward = (kV * goalRPM) + kS;
            double error = goalRPM - shooter.getRPM();
            double feedBack = error * kP;
            double power = feedForward + feedBack;







            shooter.setShooterMotorPowers(power);

            driveProcesses.intakeDriveUpdate();


            telemetry.addData("kP: ", "%.6f", kP);
            telemetry.addData("currentRPM:", shooter.getRPM());
            telemetry.addData("targetRPM:", goalRPM);
            telemetry.addData("ticksPerSec", shooter.getTicksPerSec());
            telemetry.addData("Distance: ", dis);
            telemetry.update();

        }
    }
}
