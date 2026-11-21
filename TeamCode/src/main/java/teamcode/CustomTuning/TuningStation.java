package teamcode.CustomTuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.List;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.Robot;
import teamcode.Subsystems.Turret;
import teamcode.Utility.DashboardField;
import teamcode.pedro.Constants;

/**
 * Turret + shooter tuning. Everything is set in FTC Dashboard (http://192.168.43.1:8080/dash),
 * group "TuningStation". Gains live in their own groups: "Shooter" (kS / kV / kP) and "Turret".
 *
 * BEFORE PRESSING START
 *   Set START_X, START_Y, START_HEADING_DEG to where the robot is sitting, and ALLIANCE.
 *   The start pose is read once when you press start.
 *
 * SHOOTER (graph "rpm" vs "target rpm")
 *   FLYWHEEL_ON = true, set TARGET_RPM.
 *   1. Shooter kP = 0, kV = 0. Raise kS until the flywheel just starts turning.
 *   2. Raise kV until "rpm" settles close to "target rpm" by itself.
 *   3. Raise kP until it recovers fast after a shot without oscillating.
 *   HOOD_ON / HOOD_POS and GATE_ON let you take real shots (gamepad 1 left trigger).
 *
 * TURRET (TURRET_MODE)
 *   OFF ........ both servos limp, turn the turret by hand
 *   LEFT_ONLY .. only the left servo gets TURRET_RAW_POS; the right one is limp
 *   RIGHT_ONLY . only the right servo gets TURRET_RAW_POS; the left one is limp
 *   BOTH_RAW ... left = TURRET_RAW_POS, right = TURRET_RAW_POS + Turret.rightOffsetPos
 *   FIXED_DEG .. turret points at TURRET_FIXED_DEG (robot-relative, + = left)
 *   AUTO_AIM ... turret aims at ALLIANCE's goal from the robot's position
 *   Steps:
 *   1. LEFT_ONLY: find the position where the turret points straight forward -> Turret.centerPos.
 *      Go to both ends to find servoRangeDeg / travel. Check + turns it left (else invertDirection).
 *   2. RIGHT_ONLY: same center? If it turns the opposite way, Turret.reverseRight is wrong.
 *   3. BOTH_RAW at centerPos: nudge Turret.rightOffsetPos until "hub current A" is lowest and
 *      the servos stop buzzing.
 *   4. FIXED_DEG, then AUTO_AIM.
 *
 * GAMEPAD 1
 *   Sticks .......... drive
 *   Left trigger .... shoot (flywheel on, gate opens if GATE_ON, intake feeds)
 *   Right trigger ... collect        Right bumper ... eject
 */
@Config
@TeleOp(name = "TuningStation", group = "Tuning")
public class TuningStation extends LinearOpMode {

    // ---- Start position (inches, degrees) and alliance ----
    public static double START_X = 72;
    public static double START_Y = 72;
    public static double START_HEADING_DEG = 90;
    public static PoseStorage.Alliance ALLIANCE = PoseStorage.Alliance.RED;

    // ---- Shooter ----
    public static boolean FLYWHEEL_ON = false;
    public static int TARGET_RPM = 2240;
    public static double READY_RPM_TOLERANCE = 40;
    /** false = the hood servo is never written (stays limp). */
    public static boolean HOOD_ON = false;
    public static double HOOD_POS = 0.5;
    /** false = the gate servo is never written (stays limp). */
    public static boolean GATE_ON = false;

    // ---- Turret ----
    public enum TurretMode { OFF, LEFT_ONLY, RIGHT_ONLY, BOTH_RAW, FIXED_DEG, AUTO_AIM }
    public static TurretMode TURRET_MODE = TurretMode.OFF;
    public static double TURRET_RAW_POS = 0.5;
    public static double TURRET_FIXED_DEG = 0;

    private final PoseStorage poseStorage = new PoseStorage();

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap, false); // false = turret servos limp until a mode turns them on
        Follower follower = Constants.create(hardwareMap);
        List<LynxModule> hubs = hardwareMap.getAll(LynxModule.class);
        FtcDashboard dashboard = FtcDashboard.getInstance();

        telemetry.addLine("Set START_X / START_Y / START_HEADING_DEG and ALLIANCE in Dashboard, then press start.");
        telemetry.update();
        waitForStart();

        follower.setPose(new Pose(START_X, START_Y, Math.toRadians(START_HEADING_DEG)));
        TurretMode poweredMode = null;

        try {
            while (opModeIsActive()) {
                poseStorage.setAlliance(ALLIANCE);

                // ---------- position ----------
                follower.update();
                Pose pose = follower.pose();
                double x = pose.x(), y = pose.y(), h = pose.heading();
                poseStorage.savePoseToStorage(x, y, h);
                robot.turret.setMeasuredVelocity(follower.velocity().vx, follower.velocity().vy, follower.velocity().omega);

                // ---------- turret ----------
                if (TURRET_MODE != poweredMode) {
                    // only switch servo signals when the mode changes
                    boolean left = TURRET_MODE != TurretMode.OFF && TURRET_MODE != TurretMode.RIGHT_ONLY;
                    boolean right = TURRET_MODE != TurretMode.OFF && TURRET_MODE != TurretMode.LEFT_ONLY;
                    robot.turret.setPowered(left, right);
                    poweredMode = TURRET_MODE;
                }
                runTurret(robot, x, y, h);

                // ---------- shooter ----------
                boolean shoot = gamepad1.left_trigger > 0.1;
                robot.shooter.setBaseTargetRPM(FLYWHEEL_ON || shoot ? TARGET_RPM : 0);
                robot.shooter.calculateShooterPID();
                double rpm = robot.shooter.getRPM();
                boolean atSpeed = robot.shooter.getTargetRPM() > 0
                        && Math.abs(robot.shooter.getTargetRPM() - rpm) < READY_RPM_TOLERANCE;

                if (HOOD_ON) robot.turretHood.setHoodAngle(HOOD_POS);

                // ---------- gate / intake ----------
                if (shoot) {
                    if (GATE_ON) robot.turretGate.moveGateToOpenPos();
                    robot.intake.collect();
                } else {
                    if (GATE_ON) robot.turretGate.moveGateToClosePos();
                    if (gamepad1.right_bumper) robot.intake.eject();
                    else if (gamepad1.right_trigger > 0.1) robot.intake.collect();
                    else robot.intake.stop();
                }

                // ---------- drive ----------
                robot.drivetrain.updateDriveMotorPowers(gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);

                // ---------- Dashboard graphs + field ----------
                TelemetryPacket packet = new TelemetryPacket();
                packet.put("rpm", rpm);
                packet.put("target rpm", robot.shooter.getTargetRPM());
                packet.put("rpm error", robot.shooter.getTargetRPM() - rpm);
                packet.put("flywheel power", robot.shooter.getLastPower());
                packet.put("battery V", robot.shooter.getBatteryVoltage());
                packet.put("turret cmd deg", robot.turret.getTurretHeading());
                packet.put("turret actual deg", robot.turret.getActualDeg());
                packet.put("hub current A", totalCurrent(hubs));
                packet.put("distance to target", robot.turret.distanceFromPivot(x, y, h));
                DashboardField.draw(packet.fieldOverlay(), x, y, h, robot.turret);
                dashboard.sendTelemetryPacket(packet);

                // ---------- Driver Station ----------
                telemetry.addData("rpm (actual / target)", "%.0f / %d", rpm, robot.shooter.getTargetRPM());
                telemetry.addData("flywheel at speed", atSpeed);
                telemetry.addData("turret mode", TURRET_MODE);
                telemetry.addData("turret deg (cmd / actual)", "%.1f / %.1f",
                        robot.turret.getTurretHeading(), robot.turret.getActualDeg());
                telemetry.addData("turret on target", robot.turret.isOnTarget());
                telemetry.addData("position", "%.1f, %.1f, %.0f deg", x, y, Math.toDegrees(h));
                telemetry.addData("alliance", ALLIANCE);
                telemetry.update();
            }
        } finally {
            robot.stop();
        }
    }

    /** Sends TURRET_MODE's command to the turret servos. */
    private void runTurret(Robot robot, double x, double y, double h) {
        switch (TURRET_MODE) {
            case LEFT_ONLY:
            case RIGHT_ONLY:
                robot.turret.writeRaw(TURRET_RAW_POS, TURRET_RAW_POS); // the limp servo is skipped
                break;
            case BOTH_RAW:
                robot.turret.writeRaw(TURRET_RAW_POS, TURRET_RAW_POS + Turret.rightOffsetPos);
                break;
            case FIXED_DEG:
                robot.turret.updateTurretTarget(y);
                robot.turret.setAngleDegrees(TURRET_FIXED_DEG);
                break;
            case AUTO_AIM:
                robot.turret.autoAim(x, y, h);
                break;
            default: // OFF: nothing is written
                robot.turret.updateTurretTarget(y);
                break;
        }
    }

    /** Total current draw of all hubs. Two turret servos fighting each other show up as a jump. */
    private static double totalCurrent(List<LynxModule> hubs) {
        double amps = 0;
        for (LynxModule hub : hubs) amps += hub.getCurrent(CurrentUnit.AMPS);
        return amps;
    }
}
