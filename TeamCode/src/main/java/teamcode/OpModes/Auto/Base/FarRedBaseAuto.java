package teamcode.OpModes.Auto.Base;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

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
public class FarRedBaseAuto extends LinearOpMode {


    private Follower follower;
    private Robot robot;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    // -----
    // Poses
    // -----
    private final Pose startPos = poseFactory.of(85, 139, 270);
    private final Pose farCellShootPos = poseFactory.of(85, 109, 270);
    private final Pose farShootToParkControl = poseFactory.of(129, 106, 0); //control point, so irrelevant heading
    private final Pose park = poseFactory.of(134, 40, 270);

    // -------
    // Paths
    // -------
    private Path startToFarRedShoot() {
        return line(startPos, farCellShootPos).linear(startPos, farCellShootPos);
    }
    private Path farRedCellToPark() {
        return curve(farCellShootPos, farShootToParkControl, park).linear(farCellShootPos, park);
    }

    // ---------
    // Commands
    // ---------
    private Command goToFarRed() {
        return follow(follower, startToFarRedShoot());
    }

    private Command goToParkFromFarCell() {
        return follow(follower, farRedCellToPark());
    }

    // ------------
    // AutoRoutine
    // ------------
    private Command autoRoutine(){
        return sequential(
                goToFarRed(),
                //add shoot
                //robot.shooter.basicShoot(),
                goToParkFromFarCell()
        );
    }




    @Override
    public void runOpMode() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        robot = new Robot(hardwareMap);
        follower.setPose(startPos);
        Scheduler.schedule(autoRoutine());
        waitForStart();
        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();

            telemetry.update();
            telemetry.addData("Robot PosX", follower.pose().x());
            telemetry.addData("Robot PosY", follower.pose().y());
            telemetry.addData("Robot Heading", follower.pose().heading());

        }


    }
}
