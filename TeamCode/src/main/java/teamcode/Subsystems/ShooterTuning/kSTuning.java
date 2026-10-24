package teamcode.Subsystems.ShooterTuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.dashboard.config.Config;


import teamcode.RobotContainer.Robot;

@Config
@TeleOp
public class kSTuning extends LinearOpMode {

    public static double kS = 0.06;


    //{0.00001, 0.0001, 0.001, 0.01};

    public void runOpMode(){
        Robot robot = new Robot(hardwareMap);

        waitForStart();


        while(opModeIsActive()){
            robot.shooter.setShooterMotorPowers(kS);


            telemetry.addData("kS: ", "%.6f", kS);
            telemetry.addData("currentRPM: ", robot.shooter.getRPM());
            telemetry.addData("ticksPerSec", robot.shooter.getTicksPerSec());
            telemetry.update();

        }
    }



}
