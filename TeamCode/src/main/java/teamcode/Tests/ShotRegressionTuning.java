package teamcode.Tests;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;

/**
 * Shows distance, RPM and hood so you can build the ShotRegression table in Desmos.
 *
 * SETUP (FTC Dashboard, group "ShotRegressionTuning")
 *   START_X / START_Y / START_HEADING_DEG = where you place the robot before pressing start.
 *   TARGET_X / TARGET_Y = the goal you're shooting at.
 *   TARGET_RPM and HOOD_POS = change these live until shots score, then write down
 *   distance, RPM and hood as one row in Desmos.
 *
 * GAMEPAD 1
 *   Sticks ......... drive
 *   Left trigger ... shoot (gate open + intake)
 *   Right trigger .. collect
 */
@Config
@TeleOp(name = "ShotRegressionTuning", group = "Tuning")
public class ShotRegressionTuning extends LinearOpMode {

    public static double START_X = 72;
    public static double START_Y = 72;
    public static double START_HEADING_DEG = 90;

    // default = red high goal (Turret.redHighX / redHighY)
    public static double TARGET_X = 84.79;
    public static double TARGET_Y = 87.32;

    public static int TARGET_RPM = 2240;
    public static double HOOD_POS = 0.5;

    @Override
    public void runOpMode() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        Robot robot = new Robot(hardwareMap, false); // false = turret servos stay limp
        Follower follower = Constants.create(hardwareMap);

        telemetry.addLine("Set START_X / START_Y / START_HEADING_DEG in Dashboard, then press start.");
        telemetry.update();
        waitForStart();

        follower.setPose(new Pose(START_X, START_Y, Math.toRadians(START_HEADING_DEG)));

        try {
            while (opModeIsActive()) {
                follower.update();
                double distance = distanceToTarget(follower.pose());

                // flywheel + hood from the Dashboard values
                robot.shooter.setBaseTargetRPM(TARGET_RPM);
                robot.shooter.calculateShooterPID();
                robot.turretHood.setHoodAngle(HOOD_POS);

                // shoot / collect
                if (gamepad1.left_trigger > 0.1) {
                    robot.turretGate.moveGateToOpenPos();
                    robot.intake.collect();
                } else {
                    robot.turretGate.moveGateToClosePos();
                    if (gamepad1.right_trigger > 0.1) robot.intake.collect();
                    else robot.intake.stop();
                }

                robot.drivetrain.updateDriveMotorPowers(gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);

                telemetry.addData("distance (in)", "%.1f", distance);
                telemetry.addData("rpm (actual / target)", "%.0f / %d", robot.shooter.getRPM(), TARGET_RPM);
                telemetry.addData("hood", "%.3f", robot.turretHood.getTargetPos()); // after min/max clip
                telemetry.update();
            }
        } finally {
            robot.stop();
        }
    }

    /** Straight-line distance (inches) from the robot's center to the target. */
    private double distanceToTarget(Pose pose) {
        return Math.hypot(TARGET_X - pose.x(), TARGET_Y - pose.y());
    }
}
