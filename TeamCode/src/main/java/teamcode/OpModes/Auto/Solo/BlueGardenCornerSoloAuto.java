package teamcode.OpModes.Auto.Solo;

import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.parallel;
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
import com.qualcomm.robotcore.util.ElapsedTime;

import teamcode.Utility.DashboardField;
import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.Robot;
import teamcode.RobotContainer.SubsystemCommands;
import teamcode.pedro.Constants;

/**
 * Red solo auto from the garden corner, start (133.9, 107.1, 180).
 * Drives to the first shooting spot and shoots the preloads, collects from the garden,
 * then drives to the park-side spot and shoots again. Parking isn't written yet.
 * The flywheel PID and turret aim run the whole time; position is saved for TeleOp.
 */
@Autonomous
public class BlueGardenCornerSoloAuto extends LinearOpMode {


    Follower follower;
    Robot robot;
    PoseStorage poseStorage;
    SubsystemCommands subsystemCommands;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose startRedGarden = poseFactory.of(78.8167, 134.2036, 90);
    private final Pose startGardenToGarden = poseFactory.of(134.0595, 134.3078, 0);
    private final Pose gardenToParkShoot = poseFactory.of(81.8246, 26.1364, 0);
    private final Pose gardenToParkShootControl1 = poseFactory.of(105.7459, 115.062, 0);
    private final Pose gardenToParkShootControl2 = poseFactory.of(121.1043, 31.0651, 0);
    private final Pose gardenToParkShootControl3 = poseFactory.of(111.5164, 33.8567, 0);
    private final Pose parkShootToGardenFlowerStart = poseFactory.of(81.8246, 26.1364, 0);
    private final Pose parkShootToGardenFlower = poseFactory.of(134.9914, 94.347, 0);
    private final Pose parkShootToGardenFlowerControl1 = poseFactory.of(131.981, 26.9276, 0);
    private final Pose parkShootToGardenFlowerControl2 = poseFactory.of(92.6256, 100.0377, 0);
    private final Pose gardenFlowerToGardenShoot = poseFactory.of(88.4267, 109.3291, -17.8355);
    private final Pose gardenShootToFlowerMidpoint = poseFactory.of(93.7611, 29.9783, 270);
    private final Pose gardenShootToFlowerMidpointControl1 = poseFactory.of(138.3025, 92.6328, 0);
    private final Pose parkFlowerMidpointToParkFlower = poseFactory.of(94.2441, 4.6697, -88.9067);
    private final Pose parkFlowerToSecondParkShoot = poseFactory.of(99.7753, 23.0273, -106.7678);
    private final Pose secondParkShootToPark = poseFactory.of(126.6686, 37.3446, -151.9703);

    public Path startGardenToGarden() {
        return Paths.line(startRedGarden, startGardenToGarden).constant(startGardenToGarden);
    }

    public Path gardenToParkShoot() {
        return Paths.curve(startGardenToGarden, gardenToParkShootControl1, gardenToParkShootControl2, gardenToParkShootControl3, gardenToParkShoot).constant(gardenToParkShoot);
    }

    public Path parkShootToGardenFlower() {
        return Paths.curve(parkShootToGardenFlowerStart, parkShootToGardenFlowerControl1, parkShootToGardenFlowerControl2, parkShootToGardenFlower).linear(parkShootToGardenFlowerStart, parkShootToGardenFlower);
    }

    public Path gardenFlowerToGardenShoot() {
        return Paths.line(parkShootToGardenFlower, gardenFlowerToGardenShoot).reverseTangent();
    }

    public Path gardenShootToFlowerMidpoint() {
        return Paths.curve(gardenFlowerToGardenShoot, gardenShootToFlowerMidpointControl1, gardenShootToFlowerMidpoint).constant(gardenShootToFlowerMidpoint);
    }

    public Path parkFlowerMidpointToParkFlower() {
        return Paths.line(gardenShootToFlowerMidpoint, parkFlowerMidpointToParkFlower).tangent();
    }

    public Path parkFlowerToSecondParkShoot() {
        return Paths.line(parkFlowerMidpointToParkFlower, parkFlowerToSecondParkShoot).reverseTangent();
    }

    public Path secondParkShootToPark() {
        return Paths.line(parkFlowerToSecondParkShoot, secondParkShootToPark).reverseTangent();
    }















    public Command autoRoutine() {

        return parallel(


                subsystemCommands.updateShooterRPM(),
                //subsystemCommands.updateHood(),


                //add turret and calculate RPM and Hood
                sequential(
                        //need to plug in RPM Calculator here (actually in the underlying
                        //command (which should also be moved to the root method)

                        //FIRST SHOOT ACTION
                        //add turret odometry tracking (auto-aim) and shoot from start
                        //shoot (open gate, intake)

                        sequential(

                            // update turret position
                            //RPM is already updating
                            //update hood
                            subsystemCommands.openGate(),
                            subsystemCommands.fullIntakeCommand()
                            ),

                        //keep this intake for gardenIntake
                        //subsystemCommands.zeroIntakeCommand(),
                        subsystemCommands.zeroShooterRPM(),
                        subsystemCommands.closeGate(),

                        follow(follower, startGardenToGarden()),
                        waitMs(500),
                        subsystemCommands.zeroIntakeCommand(),
                        follow(follower, gardenToParkShoot()),
                        sequential(

                                subsystemCommands.fullIntakeCommand(),
                                subsystemCommands.openGate()

                        ),
                        waitMs(500),

                        subsystemCommands.closeGate(),
                        //fullIntake is still on for flower intake

                        subsystemCommands.activateMatchLoader(),


                        follow(follower, parkShootToGardenFlower()),
                        waitMs(500),

                        subsystemCommands.restMatchLoader(),



                        follow(follower, gardenFlowerToGardenShoot()),

                        subsystemCommands.openGate(),
                        waitMs(500),
                        subsystemCommands.closeGate(),

                        follow(follower, gardenShootToFlowerMidpoint()),
                        subsystemCommands.activateMatchLoader(),
                        //fullIntake should still be on (add separate subsystem commands with a
                        //  weaker transfer intake power and a strong front intake power

                        follow(follower, parkFlowerMidpointToParkFlower()),
                        waitMs(500),

                        follow(follower, parkFlowerToSecondParkShoot()),
                        subsystemCommands.restMatchLoader(),

                        subsystemCommands.openGate(),
                        waitMs(500),

                        subsystemCommands.closeGate(),
                        subsystemCommands.zeroIntakeCommand(),

                        follow(follower, secondParkShootToPark())
                ));



                        //go to garden

                        // go to park shoot 1
                        // shoot and turret auto-aim

                        //go to garden flower

                        // go to garden shoot
                        //turret auto-aim and shoot

                        //go to park flower

                        // go to park shoot 2

                        // go to park




    }









    public void runOpMode(){
        Scheduler.reset();

        robot = new Robot(hardwareMap);
        try {
        poseStorage = new PoseStorage();
        subsystemCommands = new SubsystemCommands(hardwareMap, robot);

        ElapsedTime timer = new ElapsedTime();

        poseStorage.setAlliance(PoseStorage.Alliance.BLUE);

        follower = Constants.create(hardwareMap);
        follower.setPose(startRedGarden);

        Scheduler.schedule(autoRoutine());

        waitForStart();

        timer.reset();

        while(opModeIsActive()){

            Scheduler.execute();
            follower.update();
            poseStorage.savePoseToStorage(follower.pose().x(), follower.pose().y(), follower.pose().heading());

            DashboardField.send(follower.pose().x(), follower.pose().y(), follower.pose().heading(), robot.turret);
            //add concurrent PID methods

            robot.shooter.calculateShooterPID();


            robot.turret.updateTurretTarget(follower.pose().y());
            robot.turret.setMeasuredVelocity(follower.velocity().vx, follower.velocity().vy, follower.velocity().omega);
            robot.turret.autoAim(follower.pose().x(), follower.pose().y(), follower.pose().heading());



            telemetry.update();


        }
        } finally {
            robot.stop();
        }
    }













    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose startRedGarden = poseFactory.of(133.8808, 107.1101, 180);
    private final Pose startToFirstShoot = poseFactory.of(91, 126, 265);
    private final Pose firstShootToGardenStart = poseFactory.of(91, 126, 0);
    private final Pose firstShootToGarden = poseFactory.of(133.3849, 134.1465, 0);
    private final Pose firstShootToGardenControl1 = poseFactory.of(66, 141, 0);
    private final Pose gardenToParkShootStart = poseFactory.of(133.3849, 134.1465, 0);
    private final Pose gardenToParkShoot = poseFactory.of(81.5004, 18.7214, 0);
    private final Pose gardenToParkShootControl1 = poseFactory.of(73.0562, 141.5, 0);
    private final Pose parkShootToGardenFlower = poseFactory.of(132.1386, 100.2068, 335);
    private final Pose parkShootToGardenFlowerControl1 = poseFactory.of(70.0464, 139.0067, 0);
    private final Pose gardenFlowerToFlowerShootStart = poseFactory.of(132.1386, 100.2068, 0);
    private final Pose gardenFlowerToFlowerShoot = poseFactory.of(90.572, 119.3808, 260);
    private final Pose point6Start = poseFactory.of(90.572, 119.3808, 180);
    private final Pose point6 = poseFactory.of(132.6193, 50.4211, 180);

    // ------
    // PATHS
    // ------

    public Path startToFirstShootPath() {
        return Paths.line(startRedGarden, startToFirstShoot).linear(startRedGarden, startToFirstShoot);
    }

    public Path firstShootToGardenPath() {
        return Paths.curve(firstShootToGardenStart, firstShootToGardenControl1, firstShootToGarden).linear(firstShootToGardenStart, firstShootToGarden);
    }

    public Path gardenToParkShootPath() {
        return Paths.curve(gardenToParkShootStart, gardenToParkShootControl1, gardenToParkShoot).linear(gardenToParkShootStart, gardenToParkShoot);
    }

    public Path parkShootToGardenFlowerPath() {
        return Paths.curve(gardenToParkShoot, parkShootToGardenFlowerControl1, parkShootToGardenFlower).constant(parkShootToGardenFlower);
    }

    public Path gardenFlowerToFlowerShootPath() {
        return Paths.line(gardenFlowerToFlowerShootStart, gardenFlowerToFlowerShoot).linear(gardenFlowerToFlowerShootStart, gardenFlowerToFlowerShoot);
    }

    public Path flowerShootToParkPath() {
        return Paths.line(point6Start, point6).linear(point6Start, point6);
    }

    // ---------
    // Commands
    // ---------

    public Command goToFirstShoot(){
        return follow(follower, startToFirstShootPath());
    }

    public Command goToGarden(){
        return follow(follower, firstShootToGardenPath());
    }

    public Command goToParkShoot(){
        return follow(follower, gardenToParkShootPath());
    }

    public Command goToGardenFlower(){
        return follow(follower, parkShootToGardenFlowerPath());
    }

    public Command goToFlowerShoot(){
        return follow(follower, gardenFlowerToFlowerShootPath());
    }

    public Command park(){
        return follow(follower, flowerShootToParkPath());
    }






}
