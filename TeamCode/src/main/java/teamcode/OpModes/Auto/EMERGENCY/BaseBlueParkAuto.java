package teamcode.OpModes.Auto.EMERGENCY;

import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.acmerobotics.dashboard.config.Config;
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
import teamcode.RobotContainer.SubsystemCommands;
import teamcode.pedro.Constants;

@Config
@Autonomous
public class BaseBlueParkAuto extends LinearOpMode {

    private SubsystemCommands subsystemCommands;
    private Robot robot;
    Follower follower;

    public static int TARGET_RPM = 0;



    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(130.8677, 7.2293, 90);
    private final Pose startToParkShoot = poseFactory.of(99.4893, 24.5496, 130);
    private final Pose parkShootToPark = poseFactory.of(130.5404, 24.9932, -179.1817);

    public Path startToParkShoot() {
        return Paths.line(start, startToParkShoot).linear(start, startToParkShoot);
    }

    public Path parkShootToPark() {
        return Paths.line(startToParkShoot, parkShootToPark).reverseTangent();
    }


    public Command autoRoutine(){
        return sequential(

                follow(follower, startToParkShoot()),
                waitMs(5000),
                subsystemCommands.openGate(),
                subsystemCommands.fullIntakeCommand(),
                waitMs(500),
                subsystemCommands.closeGate(),
                subsystemCommands.zeroIntakeCommand(),
                follow(follower, parkShootToPark())


        );


    }

    public void runOpMode() {
        robot = new Robot(hardwareMap);
        subsystemCommands = new SubsystemCommands(hardwareMap, robot);

        follower = Constants.create(hardwareMap);
        follower.setPose(start);

        Scheduler.schedule(autoRoutine());

        waitForStart();

        Scheduler.reset();
        while(opModeIsActive()){

            follower.update();
            Scheduler.execute();

            robot.shooter.setBaseTargetRPM(TARGET_RPM);
            robot.shooter.calculateShooterPID();

            //follower heading may already be in degrees
            telemetry.addData("Pose: ", new Pose(follower.pose().x(), follower.pose().y(), Math.toDegrees(follower.pose().heading())));
            telemetry.addData("CurrentRPM: ", robot.shooter.getRPM());
            telemetry.update();
        }

    }

}
