package teamcode.RobotContainer;

import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.Subsystems.Intake;
import teamcode.Subsystems.Microsystems.TurretGate;
import teamcode.Subsystems.Shooter;
import teamcode.Subsystems.Turret;
import teamcode.Subsystems.Drivetrain;
import teamcode.Subsystems.Vision;

public class Robot {

    public enum Alliance {RED, BLUE, NONE}
    private Alliance alliance = Alliance.NONE;

    //public Follower follower;


    public Drivetrain drivetrain;
    public Vision vision;
    public Intake intake;
    public Shooter shooter;
    public Turret turret;
    public TurretGate turretGate;

    //intake & transfer is 2nd
    //drive & shoot is 1st



    public Robot(HardwareMap hardwareMap) {

        drivetrain = new Drivetrain(hardwareMap);
        vision = new Vision(hardwareMap);
        intake = new Intake(hardwareMap);
        shooter = new Shooter(hardwareMap);
        turret = new Turret(hardwareMap);

        /*
        Eventually create discrete Subsystems and Microsystems 'hub' classes to keep the objects more organized. I
        am not doing that for meet 1 because it will take time I don't have.
        */

        turretGate = new TurretGate(hardwareMap);


    }

    //figure out a way to either make the follower public in this file or make follower public in
    // the op mode files and allow the advanced shoot formula methods to access the robot position






    public void setAlliance(Alliance newAlliance){
        alliance = newAlliance;
    }
    public Alliance getAlliance(){return alliance;}

    //public void initRobotFollower(HardwareMap hardwareMap, Pose startPos){
    //    follower = Constants.create(hardwareMap);
    //    follower.setPose(startPos);
    //}


}
