package teamcode.Subsystems.TurretTuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import teamcode.RobotContainer.Robot;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;


@TeleOp
public class TurretTuner extends LinearOpMode {


    private final double turretGearRatio = (double) 45 /155;
    public static double kP = 0.0001;
    public static double kD = 0.00000001;
    private double lastError = 0;

    private final ElapsedTime timer = new ElapsedTime();


    public DcMotorEx turretPinionMotor;
    public Robot robot;



    public void runOpMode(){

        turretPinionMotor = hardwareMap.get(DcMotorEx.class, "turretPinion");
        robot = new Robot(hardwareMap);


        waitForStart();
        while (opModeIsActive()){
            robot.vision.scanForClusters();

            AprilTagClusterDetection clustID = robot.vision.getCurrentCluster();


            double deltaTime = timer.seconds();
            timer.reset();

            if(clustID == null){
                turretPinionMotor.setPower(0);
                lastError = 0;
                return;
            }


            double goalX = 0;
            double error = goalX - clustID.ftcPose.bearing;
            double pTerm = error * kP;

            double dTerm = 0;
            if(deltaTime > 0){
                dTerm = (error - lastError)/deltaTime * kD;
            }

            //adjust the headingOffsetTolerance
            double headingOffsetTolerance = 0.4;

            double power = 0;
            if(Math.abs(error) < headingOffsetTolerance){
                power = 0;

            } else {
                //tune this
                //tune the MAX_POWER
                double MAX_POWER = 0.5;
                double MIN_POWER = -0.5;
                power = Range.clip(pTerm + dTerm, MIN_POWER, MAX_POWER);
            }


            turretPinionMotor.setPower(power);
            lastError = error;





        }




    }












}
