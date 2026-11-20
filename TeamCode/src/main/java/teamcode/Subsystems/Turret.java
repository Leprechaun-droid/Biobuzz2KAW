package teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import teamcode.ComplexMath.Storage.PoseStorage;

public class Turret {


    PoseStorage poseStorage = new PoseStorage();
    PoseStorage.Alliance alliance;

    Servo turretServo1;
    Servo turretServo2;

    //set servoStartPos to 0.5;
    private static final double CENTER_SERVO_POS = 0.5;
    private static final double SERVO_LIMIT_POS = 0.2;
    private static final double MAX_SERVO_ROTATION_DEG = 350;
    //tune this
    private static final double TURRET_LIMIT_DEG = 90;

    private double targetServoPos;

    //fill in actual teeth amounts
    private static final double servoGearTeeth = 80;
    private static final double miniGearTeeth = 20;
    private static final double pinionGearTeeth = 5;
    private static final double turntableGearTeeth = 5;
    private static final double GEAR_RATIO = (servoGearTeeth / miniGearTeeth) * (turntableGearTeeth / pinionGearTeeth);

    //these are set equal so that, when the joystick is fully forward or backward in manual mode, the turret is on the farthest end
    // of the specified side
    private static final double JOYSTICK_GAIN = SERVO_LIMIT_POS;

    public Turret(HardwareMap hardwareMap){
        //perspective is when staring at the back of the robot
        turretServo1 = hardwareMap.get(Servo.class, "turretServoLeft");
        turretServo2 = hardwareMap.get(Servo.class, "turretServoRight");


        turretServo2.setDirection(Servo.Direction.REVERSE);

    }



    String targetCell;
    private double turretTargetX;
    private double turretTargetY;

    public void updateTurretTarget(double y, double x){
        alliance = poseStorage.getAlliance();


        //add algorithm that changes it from shooting into each side of the cell based on the x value (If it has crossed the
        // centerX of the cell (it should shoot from


        // Target coordinates based on alliance and position
        if(alliance == PoseStorage.Alliance.RED) {
            if(y < 72) {
                targetCell = "RED_GARDEN";
                turretTargetX = 84.79;
                turretTargetY = 87.32;
                if(x > 85){
                    targetCell = "RED_GARDEN_LEFT";
                    turretTargetX = 0;
                    turretTargetY = 0;
                }
            } else {
                targetCell = "RED_PARK";
                turretTargetX = 83.84;
                turretTargetY = 50.27;
            }
        } else if(alliance == PoseStorage.Alliance.BLUE) {
            if(y < 72) {
                targetCell = "BLUE_PARK";
                turretTargetX = 58.05;
                turretTargetY = 86.41;

            } else {
                targetCell = "BLUE_GARDEN";
                turretTargetX = 57.82;
                turretTargetY = 53.41;

            }
        } else {
            //set center as default target if no other targets are working
            turretTargetX = 72;
            turretTargetY = 72;
        }
    }




    public void calculateTurretServoPos(double posX, double posY, double robotHeading){
        double disX = turretTargetX - posX;
        double disY = turretTargetY - posY;


        // 1. Calculate field-centric bearing
        double targetFieldBearing = Math.toDegrees(Math.atan2(disY, disX));

        double turretTargetDegree = Range.clip(limitTurretAngleTo180Deg(targetFieldBearing - Math.toDegrees(robotHeading)), -TURRET_LIMIT_DEG, TURRET_LIMIT_DEG);

        double baseTargetServoPos = ((turretTargetDegree * GEAR_RATIO) / MAX_SERVO_ROTATION_DEG) + CENTER_SERVO_POS;

        // final safety so setPosition never gets a value outside 0–1
        targetServoPos = Range.clip(baseTargetServoPos, 0, 1);

    }

    public void setTurretServoPos(){
        turretServo1.setPosition(targetServoPos);
        turretServo2.setPosition(targetServoPos);
    }

    public void updateServoTurret(double posX, double posY, double robotHeading ){
        updateTurretTarget(posY);
        calculateTurretServoPos(posX, posY, robotHeading);
        setTurretServoPos();

    }

    public void manualTurretInCaseOfEmergency(double joystickValue){
        targetServoPos = clipToServoLimit(CENTER_SERVO_POS + joystickValue * JOYSTICK_GAIN);
    }

    private double clipToServoLimit(double targetServoPos){
        return Range.clip(targetServoPos, (CENTER_SERVO_POS - SERVO_LIMIT_POS) , (CENTER_SERVO_POS + SERVO_LIMIT_POS));
    }


    public double limitTurretAngleTo180Deg(double targetAngle){
        double newTargetAngle = targetAngle;
        while(newTargetAngle > 180 || newTargetAngle < -180){
               if(newTargetAngle > 180){
                   newTargetAngle -= 360;
               } else {
                   newTargetAngle += 360;
               }
        }
        return newTargetAngle;
    }









    //update turret gear ratio; the designers added two mirrored servo motors that connect via a small
    //  gear that attached to the same shaft as a larger gear that is then connected to a large turntable gear

    public double getTurretTargetX(){
        return turretTargetX;
    }

    public double getTurretTargetY(){
        return turretTargetY;
    }


}
