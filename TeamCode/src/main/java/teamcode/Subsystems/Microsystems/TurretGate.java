package teamcode.Subsystems.Microsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TurretGate {

    Servo turretGate;

    public TurretGate(HardwareMap hardwareMap){
        turretGate = hardwareMap.get(Servo.class, "turretGate");


    }


    private final double openGatePos = 0;
    private final double closeGatePos = 1;
    public void openGate(){
        turretGate.setPosition(openGatePos);

    }


    public void closeGate(){
        turretGate.setPosition(closeGatePos);
    }



}
