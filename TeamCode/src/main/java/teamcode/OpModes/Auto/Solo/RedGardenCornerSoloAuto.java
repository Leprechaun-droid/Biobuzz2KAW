package teamcode.OpModes.Auto.Solo;

import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.parallel;
import static com.pedropathing.ivy.groups.Groups.race;
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


    Follower follower;
    Robot robot;
    PoseStorage poseStorage;
    SubsystemCommands subsystemCommands;



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












    public Command autoRoutine() {

        return deadline(

                sequential(
                        initAuto(),
                        //need to plug in RPM Calculator here (actually in the underlying
                        //  command (which should also be moved to the root method)

                        //FIRST SHOOT ACTION
                        deadline(
                                // must run continously parallel to sequential because
                                // it uses coordinates to calculate RPM
                                // so it must be running the entire time so it
                                // will take the least amount of time at the end
                                sequential(
                                        //move to first shoot with preloaded 4 balls
                                        goToFirstShoot(),
                                        subsystemCommands.readyIntakeAndGateForShoot()
                                ),
                                subsystemCommands.updateShooterRPM()


                        ),

                        // close gate so the intake when collecting from garden does not
                        // cause it to shoot
                        subsystemCommands.closeGate(),
                        // rest the shooter
                        subsystemCommands.zeroShooterRPM(),

                        sequential(
                                // intake is still running
                                goToGarden(),
                                // wait some time to ensure we collected everything
                                waitMs(300)
                        ),
                        // stop intake so it does not overcollide with the gate
                        subsystemCommands.zeroIntakeCommand(),
                        //SECOND SHOOT ACTION
                        deadline(
                                //needs deadline or parallel will cause it to run
                                // infinitely becase the updateShooterRPM is an infinite command
                                sequential(

                                        goToParkShoot(),
                                        subsystemCommands.readyIntakeAndGateForShoot()
                                ),
                                subsystemCommands.updateShooterRPM()


                        ),
                        subsystemCommands.closeGate(),
                        deadline(

                                sequential(
                                    //intake is still running
                                    subsystemCommands.activateMatchLoader(),
                                    goToGardenFlower(),
                                    //check if this pauses intake; that would need correction
                                    // intake is supposed to be running during the wait
                                    waitMs(300)
                                ),
                                //check if this is needed; this is here
                                //  for redundancy if the waitMs pauses the intake
                                //should run continously while the sequential command group
                                //  in deadline is running
                                subsystemCommands.fullIntakeCommand()
                        ),
                        subsystemCommands.restMatchLoader(),
                        subsystemCommands.zeroIntakeCommand(),
                        //THIRD SHOOT ACTION
                        // look into if I can pass a path command as an argument and call it in a
                        //      separate command; leave for now, maybe by meet-3
                        deadline(
                                sequential(
                                        goToFlowerShoot(),
                                        //must wait until at shoot pos for redundancy, don't want
                                        //  any balls flying out accidentally
                                        subsystemCommands.readyIntakeAndGateForShoot()
                                ),

                                subsystemCommands.updateShooterRPM()

                        ),
                        prepareForAutoEnd(),
                        park()
                        ),

                        subsystemCommands.updateShooterPID()
                        //add turret here as well (or I could actually put update the shooter and the
                        // turret PID outside the auto routine as individual methods in the while loop) (will
                        // probably do this)
                        //add turret and calculate RPM as well as HoodAngle


                );






        //Pseudocode:
                //run the flywheel with an infinite command the entire
                    //time in parallel (maybe not, this could cause it to overexhert the motor)
                 //move from startPos by flower to shootPos
                //turn trans on, wait a time (roughly a second), then turn full intake on
                //after a certain amount of time that it takes to shoot all 4 balls, turn off full intake
                //move to midPos where we could collect from the garden or flower
    }






    public Command initAuto(){
        return sequential(
                subsystemCommands.zeroIntakeCommand(),

                //subsystemCommands.zeroShooterRPM(),
                subsystemCommands.restMatchLoader()
        );
    }

    public Command prepareForAutoEnd(){
        return sequential(
                subsystemCommands.closeGate(),
                subsystemCommands.zeroIntakeCommand(),
                subsystemCommands.zeroShooterRPM(),
                subsystemCommands.restMatchLoader()
        );
    }








    public void runOpMode(){
        Scheduler.reset();

        robot = new Robot(hardwareMap);
        poseStorage = new PoseStorage();
        subsystemCommands = new SubsystemCommands(hardwareMap, robot);

        poseStorage.setAlliance(PoseStorage.Alliance.RED);

        follower = Constants.create(hardwareMap);
        follower.setPose(startRedGarden);

        Scheduler.schedule(autoRoutine());

        waitForStart();


        while(opModeIsActive()){
            follower.update();
            Scheduler.execute();
            poseStorage.savePoseToStorage(follower.pose().x(), follower.pose().y(), follower.pose().heading());
            //add concurrent PID methods


            //shooterPID needs to be done inside the loop so the RPM can manually be set to 0
                // the shooterPID that calculates the ideal power can be out here, but the part
                //  of it that sets the targetRPM must be placed inside the autoRoutine

            // setHoodPos does not go outside auto because running it the entire time could strain
            //  the servo; it must be set at certain

            robot.turret.updateServoTurret(follower.pose().x(), follower.pose().y(), follower.pose().heading());
            //update turret position (this goes outside the autoRoutine (putting it inside serves no purpose))
            // it runs the same calculation continously (may need to put it in if manually setting positions)





            telemetry.addData("followerX: ", follower.pose().x());
            telemetry.addData("followerY: ", follower.pose().y());
            telemetry.addData("followerHeading(Deg): ", Math.toDegrees(follower.pose().heading()));
            // 0 is open, 1 is closed (or at least it should be)
            telemetry.addData("GateState: ", robot.turretGate.getGateState());

            telemetry.addData("poseStorageX: ", poseStorage.getPosX());
            telemetry.addData("poseStorageY: ", poseStorage.getPosY());
            telemetry.addData("poseStorageHeading(Deg): ", Math.toDegrees(poseStorage.getRobotHeading()));

            telemetry.addData("turretTargetX: ", robot.turret.getTurretTargetX());
            telemetry.addData("turretTargetY: ", robot.turret.getTurretTargetY());




            telemetry.update();


        }




    }
















}
