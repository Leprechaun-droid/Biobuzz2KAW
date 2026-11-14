package teamcode.Tests;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import teamcode.RobotContainer.Robot;

@Disabled
@TeleOp
public class ScanTest extends LinearOpMode {

    private Robot robot;

    @Override
    public void runOpMode(){

        robot = new Robot(hardwareMap);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());


        robot.vision.initVisionPortal();
        //calling two instances of visionPortal by using a get method would strain the CPU and
        //  there would be two visionPortals; this is cleaner
        robot.vision.startSteamingtoDashboard(15);

        robot.vision.resetScanner();
        telemetry.update();

        waitForStart();

        while(opModeIsActive()){



            telemetry.addData("Cluster ID", robot.vision.getCurrentCluster());
            telemetry.update();
        }
    }
}
