package teamcode.OpModes.Auto.BasePowerAuto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import teamcode.RobotContainer.Robot;


@Config
@Autonomous
public class BlueFarPrimitive extends LinearOpMode {


    private ElapsedTime timer;
    private Robot robot;
    public static double t1 = 1;
    public static double mid_t1_t2 = 2.5;
    public static double t2 = 7;
    public static double mid_m2_t3 = 9;
    public static double t3 = 12;




    public void runOpMode(){
        timer = new ElapsedTime();
        robot = new Robot(hardwareMap);

        timer.reset();
        waitForStart();

        while(opModeIsActive()) {

            if (timer.seconds() < t1) {
                robot.drivetrain.updateDriveMotorPowers(-0.55, 0, 0);
            } else if ((timer.seconds() > t1) && (timer.seconds() < mid_t1_t2)){
                robot.drivetrain.updateDriveMotorPowers(0, 0, 0);
            } else if (timer.seconds() > mid_t1_t2 && timer.seconds() < t2) {
                robot.drivetrain.updateDriveMotorPowers(0, 0.05, 0);
            } else if (timer.seconds() > t2 && timer.seconds() < mid_m2_t3){
                robot.drivetrain.updateDriveMotorPowers(0, 0, 0);
            } else if (timer.seconds() > mid_m2_t3 && timer.seconds() < t3){
                robot.drivetrain.updateDriveMotorPowers(-0.4, 0, 0);
            }
            else {
                robot.drivetrain.updateDriveMotorPowers(0, 0, 0);
            }
        /*
        else if (timer.seconds() > t1 && timer.seconds() < t2){
            robot.drivetrain.updateDriveMotorPowers(0, 0, 0.3);
        } else if(timer.seconds() > t2 && timer.seconds() < t3){
            robot.drivetrain.updateDriveMotorPowers(0.3, 0, 0);
        }


        */


            telemetry.addData("Timer", timer.seconds());
            telemetry.update();


        }
    }

}
