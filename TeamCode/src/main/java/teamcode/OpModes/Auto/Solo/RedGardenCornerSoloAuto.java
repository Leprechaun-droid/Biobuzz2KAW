package teamcode.OpModes.Auto.Solo;

import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.Robot;
import teamcode.RobotContainer.SubsystemCommands;
import teamcode.pedro.Constants;

public class RedGardenCornerSoloAuto extends LinearOpMode {
    Follower follower; //
    Robot robot;
    PoseStorage poseStorage = new PoseStorage();
    SubsystemCommands subsystemCommands;




    //check if putting it in degrees is a bad idea because it might require radians

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(62.0718, 7.5555, 180);
    private final Pose startGardenToGarden = poseFactory.of(6.0406, 8.0663, 180);
    private final Pose gardenToParkShootStart = poseFactory.of(6.0406, 8.0663, 180);
    private final Pose gardenToParkShoot = poseFactory.of(55.9461, 122.9071, 290);
    private final Pose gardenToParkShootControl1 = poseFactory.of(20.455, 4.8423, 0);
    private final Pose gardenToParkShootControl2 = poseFactory.of(19.1973, 103.8379, 0);
    private final Pose parkShootToGardenFlowerStart = poseFactory.of(55.9461, 122.9071, 290);
    private final Pose parkShootToGardenFlower = poseFactory.of(23.6305, 44.858, 160);
    private final Pose parkShootToGardenFlowerControl1 = poseFactory.of(32.1044, 105.3715, 0);
    private final Pose parkShootToGardenFlowerControl2 = poseFactory.of(12.1832, 51.098, 0);
    private final Pose parkShootToGardenFlowerControl3 = poseFactory.of(56.772, 42.1095, 0);
    private final Pose point4 = poseFactory.of(7.2378, 45.9373, 180);
    private final Pose gardenFlowerToGardenShootStart = poseFactory.of(7.2378, 45.9373, 160);
    private final Pose gardenFlowerToGardenShoot = poseFactory.of(53.6623, 14.868, 60);
    private final Pose gardenShootToParkFlowerStart = poseFactory.of(53.6623, 14.868, 60);
    private final Pose gardenShootToParkFlower = poseFactory.of(46.9532, 131.9248, 90);
    private final Pose gardenShootToParkFlowerControl1 = poseFactory.of(31.939, 34.5113, 0);
    private final Pose gardenShootToParkFlowerControl2 = poseFactory.of(6.7694, 84.1858, 0);
    private final Pose gardenShootToParkFlowerControl3 = poseFactory.of(51.7865, 102.5234, 0);
    private final Pose point7 = poseFactory.of(47.0496, 120.2703, 90);
    private final Pose parkFlowerToParkShoot2Start = poseFactory.of(47.0496, 120.2703, 90);
    private final Pose parkFlowerToParkShoot2 = poseFactory.of(42.8272, 115.9718, 350);
    private final Pose parkShoot2ToParkStart = poseFactory.of(42.8272, 115.9718, 350);
    private final Pose parkShoot2ToPark = poseFactory.of(13.2498, 107.4135, 0);

    public Path startGardenToGarden() {
        return Paths.line(start, startGardenToGarden).linear(start, startGardenToGarden);
    }

    public Path gardenToParkShoot() {
        return Paths.curve(gardenToParkShootStart, gardenToParkShootControl1, gardenToParkShootControl2, gardenToParkShoot).linear(gardenToParkShootStart, gardenToParkShoot);
    }

    public Path parkShootToGardenFlower() {
        return Paths.curve(parkShootToGardenFlowerStart, parkShootToGardenFlowerControl1, parkShootToGardenFlowerControl2, parkShootToGardenFlowerControl3, parkShootToGardenFlower).linear(parkShootToGardenFlowerStart, parkShootToGardenFlower);
    }

    public Path path4() {
        return Paths.line(parkShootToGardenFlower, point4).constant(point4);
    }

    public Path gardenFlowerToGardenShoot() {
        return Paths.line(gardenFlowerToGardenShootStart, gardenFlowerToGardenShoot).linear(gardenFlowerToGardenShootStart, gardenFlowerToGardenShoot);
    }

    public Path gardenShootToParkFlower() {
        return Paths.curve(gardenShootToParkFlowerStart, gardenShootToParkFlowerControl1, gardenShootToParkFlowerControl2, gardenShootToParkFlowerControl3, gardenShootToParkFlower).linear(gardenShootToParkFlowerStart, gardenShootToParkFlower);
    }

    public Path path7() {
        return Paths.line(gardenShootToParkFlower, point7).constant(point7);
    }

    public Path parkFlowerToParkShoot2() {
        return Paths.line(parkFlowerToParkShoot2Start, parkFlowerToParkShoot2).linear(parkFlowerToParkShoot2Start, parkFlowerToParkShoot2);
    }

    public Path parkShoot2ToPark() {
        return Paths.line(parkShoot2ToParkStart, parkShoot2ToPark).linear(parkShoot2ToParkStart, parkShoot2ToPark);
    }



















    public Command autoRoutine(){
        return sequential(

                subsystemCommands.shoot(robot.shooter.getRPM()),

                subsystemCommands.closeGate(),
                subsystemCommands.fullIntakeCommand(),

                follow(follower, startGardenToGarden()),
                waitMs(500),

                follow(follower, gardenToParkShoot()),
                subsystemCommands.shoot(robot.shooter.getRPM()),

                subsystemCommands.closeGate(),
                subsystemCommands.fullIntakeCommand(), //redundancy and reliability; should already be running

                follow(follower, parkShootToGardenFlower()),

                subsystemCommands.activateMatchLoader(),

                follow(follower, path4()),
                waitMs(500),

                subsystemCommands.restMatchLoader(),
                follow(follower, gardenFlowerToGardenShoot()),

                subsystemCommands.shoot(robot.shooter.getRPM()),

                subsystemCommands.closeGate(),

                follow(follower, gardenShootToParkFlower())

                subsystemCommands.activateMatchLoader(),
                subsystemCommands.fullIntakeCommand(), //should still be on but I want to be reminded of it

                follow(follower, path7()),
                waitMs(500),

                subsystemCommands.restMatchLoader(),
                follow(follower, parkFlowerToParkShoot2()),
                subsystemCommands.shoot(robot.shooter.getRPM()),

                subsystemCommands.closeGate(),
                subsystemCommands.zeroIntakeCommand(),

                follow(follower, parkShoot2ToPark())
















        )
    }



    public void runOpMode(){
        Scheduler.schedule(autoRoutine());
        follower = Constants.create(hardwareMap);

        follower.setPose(start);

        poseStorage.setAlliance(PoseStorage.Alliance.RED);


        Scheduler.reset();




        waitForStart();
        while(opModeIsActive()){

            Scheduler.execute();
            follower.update();
            poseStorage.savePoseToStorage(follower.pose().x(), follower.pose().y(), follower.pose().heading());


            robot.shooter.calculateShooterPID();
            //add autoRPM calculation as argument
            robot.shooter.autoRPM();








        }


    }

}
