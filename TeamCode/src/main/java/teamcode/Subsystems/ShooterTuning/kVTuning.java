package teamcode.Subsystems.ShooterTuning;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.RobotContainer.Robot;

@Config
@TeleOp
public class kVTuning extends LinearOpMode {



    public static double kV = 0.00075;
    //replace with tuned values
    public static double kS = 0.075;


    public static int goalRPM = 900;


    public void runOpMode(){
        Robot robot = new Robot(hardwareMap);

        waitForStart();


        while(opModeIsActive()){


            double power = (kV * goalRPM) + kS;

            robot.shooter.setShooterMotorPowers(power);

            telemetry.addData("kV: ", "%.6f", kV);
            telemetry.addData("TargetRPM: ", goalRPM);
            telemetry.addData("currentRPM: ", robot.shooter.getRPM());
            telemetry.addData("ticksPerSec: ", robot.shooter.getTicksPerSec());
            telemetry.addData("Power: ", power);
            telemetry.update();

        }
    }



}
