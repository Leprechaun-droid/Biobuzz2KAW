package teamcode.OpModes.Auto.Base;

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
public class NearBlueBaseAuto extends LinearOpMode {


    private Follower follower;
    private Robot robot;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    // -----
    // Poses
    // -----
    private final Pose startPos = poseFactory.of(15, 135, 270);
    // check if the distance from the cell is the same for each side. It should be relatively simple and just measure
    // the y difference
    private final Pose park = poseFactory.of(11, 120, 270);

    // -------
    // Paths
    // -------
    private Path startToPark() {
        return line(startPos, park).linear(startPos, park);
    }

    // ---------
    // Commands
    // ---------
    private Command goToPark() {
        return follow(follower, startToPark());
    }


    // ------------
    // AutoRoutine
    // ------------
    private Command autoRoutine(){
        return sequential(

                //check if cell is raised
                    //if it is, perform the following action
                    //if not, waitUntil with conditional or something similar
                        //add redundancy in case the other team cannot




                //repeat 4 times (we start with 4 preloaded pollen)
                    //set TargetRPM
                    //when at specific RPM (or in error range)
                        //openServoGate
                        // loadLauncher
                        // close servoGate
                        // zeroTransfer

                        // fullFrontIntake
                        //zeroFrontIntake





                //go to Far blue shooting pos or start there
                //hive is always tipped up on far blue, so I don't need to check aprilTags
                //shoot
                //go to park

                goToPark()
                //robot.shooter.basicShoot(),
                //Repeat 4 times
                    //if a certain RPM margin of error is reached, load a ball for shooting

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

        robot.vision.resetScanner();
        while (opModeIsActive()) {
            follower.update();
            //robot.vision.scanForClusters();
            //robot.turret.calculateTurretPIDPower(robot.vision.getCurrentCluster());
            //robot.shooter.calculateShooterPID();
            Scheduler.execute();

            telemetry.update();
            telemetry.addData("Robot PosX", follower.pose().x());
            telemetry.addData("Robot PosY", follower.pose().y());
            telemetry.addData("Robot Heading", follower.pose().heading());

        }


    }
}
