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
    public void moveGateToOpenPos(){
        //may need to add a condition not to open unless RPM is within an acceptable
            //margin of error
        turretGate.setPosition(openGatePos);

    }


    public void moveGateToClosePos() {
        turretGate.setPosition(closeGatePos);
    }

    public double getGatePos(){
        return turretGate.getPosition();
    }
    public double getOpenGatePos(){
        return openGatePos;
    }

    public double getCloseGatePos(){
        return closeGatePos;
    }




}
