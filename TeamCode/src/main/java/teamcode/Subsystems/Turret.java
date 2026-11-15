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
    double servoStartPos = 0.5;
    double targetServoPos;

    //fill in actual teeth amounts
    private final double servoGearTeeth = 80;
    private final double miniGearTeeth = 20;
    private final double pinionGearTeeth = 5;
    private final double turntableGearTeeth = 5;


    double GEAR_RATIO = (servoGearTeeth / miniGearTeeth) * (turntableGearTeeth / pinionGearTeeth);

    public Turret(HardwareMap hardwareMap){
        //perspective is when staring at the back of the robot
        turretServo1 = hardwareMap.get(Servo.class, "turretServoLeft");
        turretServo2 = hardwareMap.get(Servo.class, "turretServoRight");


        turretServo2.setDirection(Servo.Direction.REVERSE);

    }



    String targetCell;
    //check why volatile is necessary; I dont see why it is if it can just be updated for each opmode
    private static volatile double turretTargetX;
    private static volatile double turretTargetY;

    public void updateTurretTarget(double y){
        alliance = poseStorage.getAlliance();

        // Target coordinates based on alliance and position
        if(alliance == PoseStorage.Alliance.RED) {
            if(y < 72) {
                targetCell = "RED_GARDEN";
                turretTargetX = 84.79;
                turretTargetY = 87.32;

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
        double MAX_SERVO_ROTATION_DEG = 350;


        // 1. Calculate field-centric bearing
        double targetFieldBearing = Math.toDegrees(Math.atan2(disY, disX));

        double turretTargetDegree = limitTurretAngleTo180Deg((targetFieldBearing - robotHeading));

        //I think this is right; make sure there is no conflict if the limit for the axon servo is 350 degrees instead
        double baseTargetServoPos = ((turretTargetDegree * GEAR_RATIO) / MAX_SERVO_ROTATION_DEG) + servoStartPos;

        double servoUpperLimit = servoStartPos + 0.2;
        double servoLowerLimit = servoStartPos - 0.2;
        targetServoPos = Range.clip(baseTargetServoPos, servoLowerLimit, servoUpperLimit);

    }

    public void setTurretServoPos(){
        turretServo1.setPosition(targetServoPos);
        turretServo2.setPosition(targetServoPos);
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
