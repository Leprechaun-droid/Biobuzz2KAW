package teamcode.Subsystems.ShooterTuning;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.RobotContainer.DriveProcesses;
import teamcode.Subsystems.Shooter;
import teamcode.Subsystems.Turret;

@Config
@TeleOp
public class kPShooterTuning extends LinearOpMode {

    // -------------------------
    // SHOOTER
    // -------------------------
    public static double kV = 0.00075;
    public static double kS = 0.075;
    public static double kP = 0;

    //540 is a good number, it is give or take 10
    public static int goalRPM = 560;

    private Shooter shooter;

    // ------------------------------------
    // Turret
    // ------------------------------------

    private Turret turret;

    private DriveProcesses driveProcesses;



    public void runOpMode(){
        shooter = new Shooter(hardwareMap);
        turret = new Turret(hardwareMap);
        driveProcesses = new DriveProcesses(hardwareMap, gamepad1, gamepad2);

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
            telemetry.addData("currentRPM: ", shooter.getRPM());
            telemetry.addData("ticksPerSec", shooter.getTicksPerSec());
            telemetry.update();

        }
    }
}
