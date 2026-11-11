package teamcode.Subsystems.Microsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;


public class MatchLoader {


    Servo matchLoader;
    double raisePos = 0.8;
    double lowerPos = 0.2;
    public MatchLoader(HardwareMap hardwareMap) {

        matchLoader = hardwareMap.get(Servo.class, "matchLoader");

    }




    public void lowerMatchLoader(){
        matchLoader.setPosition(lowerPos);
    }

    public void raiseMatchLoader(){
        matchLoader.setPosition(raisePos);
    }




}
