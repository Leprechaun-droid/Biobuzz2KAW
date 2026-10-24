package teamcode.Subsystems.ShooterTuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.RobotContainer.DriveProcesses;
import teamcode.RobotContainer.Robot;
import teamcode.Subsystems.Shooter;
import teamcode.Subsystems.Turret;

@Config
@TeleOp
public class kPShooterTuning extends LinearOpMode {

    // -------------------------
    // SHOOTER
    // -------------------------
    public static double kV = 0.0008;
    public static double kS = 0.07;
    public static double kP = 0.005;

    //540 is a good number, it is give or take 10
    public static int goalRPM = 560;

    private Shooter shooter;

    // ------------------------------------
    // Turret
    // ------------------------------------

    private Turret turret;

    private DriveProcesses driveProcesses;

    private Robot robot;

    public void runOpMode(){
        shooter = new Shooter(hardwareMap);
        turret = new Turret(hardwareMap);
        robot = new Robot(hardwareMap);
        driveProcesses = new DriveProcesses(robot, hardwareMap, gamepad1, gamepad2);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        waitForStart();
        while(opModeIsActive()){
                double feedForward = (kV * goalRPM) + kS;
                double error = goalRPM - shooter.getRPM();
                double feedBack = error * kP;
                double power = feedForward + feedBack;


                if(gamepad1.right_bumper) {
                    goalRPM = 560;
                } else if(gamepad1.left_bumper){
                    goalRPM = 800;
                }


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
