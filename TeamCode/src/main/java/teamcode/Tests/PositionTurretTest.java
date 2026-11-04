package teamcode.Tests;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;


@TeleOp
public class PositionTurretTest extends LinearOpMode {




    @Override
    public void runOpMode(){
       Robot robot = new Robot(hardwareMap);
        Follower follower = Constants.create(hardwareMap);
        PoseStorage poseStorage = new PoseStorage();
        poseStorage.setAlliance(PoseStorage.Alliance.BLUE);

        follower.setPose(new Pose(5, 138, 270));

        waitForStart();
        while(opModeIsActive()){
            follower.update();
            poseStorage.savePoseToStorage(follower.pose().x(), follower.pose().y(), Math.toDegrees(follower.pose().heading()));

            double vertical = gamepad1.left_stick_y;
            double horizontal = gamepad1.right_stick_x;
            double strafe = gamepad1.right_stick_x;

            //robot.turret.updateTurretTarget();
            //robot.turret.calculateTurretPIDPower(follower.pose().x(), follower.pose().y(), follower.pose().heading(), robot.turret.getTurretHeading());
            //robot.turret.slipRing();

            robot.drivetrain.updateDriveMotorPowers(vertical, horizontal, strafe);



            //telemetry.addData("TurretHeading", robot.turret.getTurretHeading());
            //telemetry.addData("Alliance", poseStorage.getAlliance());
            //telemetry.addData("turretTargetX", robot.turret.getTurretTargetY());
            //telemetry.addData("turretTargetY", robot.turret.getTurretTargetY());

            telemetry.addData("turretPos", robot.turret.getTurretPos());


            telemetry.update();



        }




    }


}
