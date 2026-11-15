package teamcode.CustomTuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;
import teamcode.Subsystems.Shooter;


@Disabled
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

    public void runOpMode(){
        shooter = new Shooter(hardwareMap);
        robot = new Robot(hardwareMap);
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        waitForStart();
        while(opModeIsActive()){

            // Quick Tuning Tutorial
            //      1. set kV and kP to 0, then tune kS until the flywheel begins spinning
            //          - this establishes the basic amount of power needed to get the flywheel moving
            //      2. tune kV until the graph is bouncing up and down over the target velocity with relative accuracy
            //          - minimizes the work kP needs to do, allowing for it to be more accurate
            //      3. tune kP until this is narrowed even further


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
            telemetry.update();

        }
    }
}
