package teamcode.Subsystems.Microsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TurretHood {

    Servo turretHood;
    public TurretHood(HardwareMap hardwareMap){
        turretHood = hardwareMap.get(Servo.class, "turretHood");


    }

    public void setHoodAngle(double angle){
        turretHood.setPosition(angle);
    }

}
