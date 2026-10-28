package teamcode.RobotContainer;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.pedro.Constants;

public class CustomFollower {

















    public Follower initCustomFollower(HardwareMap hardwareMap, Pose startPos){

        Follower follower = Constants.create(hardwareMap);
        follower.setPose(startPos);


        return follower;


    }






}
