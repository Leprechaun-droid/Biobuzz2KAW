package teamcode.OpModes.Auto;

import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;

@Autonomous
public class FarRedAuto extends LinearOpMode {
    private Follower follower;
    private Robot robot;
    private PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose startPos = poseFactory.of(85, 133, 270);
    private final Pose atFarCell = poseFactory.of(85, 100, 270);
    private final Pose farCellToParkControl = poseFactory.of(133, 112, 0);
    private final Pose parkPos = poseFactory.of(133, 43, 270);

    private Path fromStartToFarCell(){
        return Paths.line(startPos, atFarCell).linear(startPos, atFarCell);
    }

    private Path farCellToPark(){
        return Paths.curve(atFarCell, farCellToParkControl, parkPos).linear(atFarCell, parkPos);
    }

    private Command goToFarCell(){
        return follow(follower, fromStartToFarCell());
    }

    private Command goToPark(){
        return follow(follower, farCellToPark());
    }

    private Command autoRoutine(){
        return sequential(goToFarCell(),

                //rev the motor
                //









                robot.shooter.basicShoot(),
                goToPark()
        );
    }






    @Override
    public void runOpMode(){
        robot = new Robot(hardwareMap);


        robot.setAlliance(Robot.Alliance.RED);
        Scheduler.reset();
        //make actually create function in the constants file
        follower = Constants.create(hardwareMap);
        //aprilTag for startPos must go before buildPaths, or buildPaths will have no values for startPos
        //set startPos manually if using AprilTags to check will not be allowed
        follower.setPose(startPos);
        Scheduler.schedule(autoRoutine());


        waitForStart();
        while(opModeIsActive()){
            follower.update();
            Scheduler.execute();












            telemetry.addData("PosX", follower.pose().x());
            telemetry.addData("PosY", follower.pose().y());
            telemetry.addData("Heading", follower.pose().heading());

            telemetry.update();
        }
    }






}
