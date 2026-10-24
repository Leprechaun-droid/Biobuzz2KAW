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
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import teamcode.RobotContainer.Robot;
import teamcode.pedro.Constants;

public class NearBlueBaseAuto extends LinearOpMode {


    private Follower follower;
    private Robot robot;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    // -----
    // Poses
    // -----
    private final Pose startPos = poseFactory.of(56, 9, 90);
    // check if the distance from the cell is the same for each side. It should be relatively simple and just measure
    // the y difference
    private final Pose nearCellShootPos = poseFactory.of(56, 34, 90);
    private final Pose nearCellShootPosControl = poseFactory.of(11, 29, 0); //reminder: irrelevant control point heading
    private final Pose park = poseFactory.of(9, 98, 90);

    // -------
    // Paths
    // -------
    private Path startToNearBlueShoot() {
        return line(startPos, nearCellShootPos).linear(startPos, nearCellShootPos);
    }
    private Path nearBlueCellToPark() {
        return curve(nearCellShootPos, nearCellShootPosControl, park).linear(nearCellShootPos, park);
    }

    // ---------
    // Commands
    // ---------
    private Command goToNearBlue() {
        return follow(follower, startToNearBlueShoot());
    }

    private Command goToParkFromNearCell() {
        return follow(follower, nearBlueCellToPark());
    }

    // ------------
    // AutoRoutine
    // ------------
    private Command autoRoutine(Robot.Alliance alliance){
        return sequential(
                goToNearBlue(),
                //add shoot
                //change 1000 to targetRPM
                //robot.vision.useAprilTagsToSetCellState(alliance),
                //robot.shooter.basicShoot(),
                goToParkFromNearCell()
        );
    }




    @Override
    public void runOpMode() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        robot = new Robot(hardwareMap);
        follower.setPose(startPos);
        robot.setAlliance(Robot.Alliance.BLUE);
        Scheduler.schedule(autoRoutine(robot.getAlliance()));
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
