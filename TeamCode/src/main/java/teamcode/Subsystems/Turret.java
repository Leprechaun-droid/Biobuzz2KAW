package teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;

import teamcode.ComplexMath.Storage.PoseStorage;
import teamcode.RobotContainer.Robot;

public class Turret {


    private double kP = 0.001;
    private double kD = 0.0001;
    double power = 0;


    private double lastError = 0;

    private final ElapsedTime timer = new ElapsedTime();

    DcMotorEx turretPinionMotor;









    public Turret(HardwareMap hardwareMap){

        turretPinionMotor = hardwareMap.get(DcMotorEx.class, "turretPinion");

        turretPinionMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        turretPinionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);




    }


    String targetCell;
    double turretTargetX;
    double turretTargetY;

    //may move Alliance to poseStorage so I dont have to deal with calling robot infinitely (possibly)

    public void updateTurretTarget(Robot robot){

        PoseStorage poseStorage = new PoseStorage();

        // redGardenCellPos = (57.82, 53.41)
        // redParkCellPos = (58.05, 86.41)
        // blueGardenCellPos = (84.79, 87.32)
        // blueParkCellPos = (83.84, 50.27)




        if(robot.getAlliance() == Robot.Alliance.RED && poseStorage.getPosY() < 72){
            //in redGarden quadrant
            targetCell = "RED_GARDEN";
            turretTargetX = 57.82;
            turretTargetY = 53.41;
            //set turret target Pos
        } else if(robot.getAlliance() == Robot.Alliance.RED && poseStorage.getPosY() >= 72){
            targetCell = "RED_PARK";
            turretTargetX = 58.05;
            turretTargetY = 86.41;

        } else if(robot.getAlliance() == Robot.Alliance.BLUE && poseStorage.getPosY() < 72){
            targetCell = "BLUE_PARK";
            turretTargetX = 83.84;
            turretTargetY = 50.27;

        } else if(robot.getAlliance() == Robot.Alliance.BLUE && poseStorage.getPosY() >= 72){
            targetCell = "BLUE_GARDEN";
            turretTargetX = 84.79;
            turretTargetY = 87.32;

        } else {
            turretTargetX = 0;
            turretTargetY = 0;
        }





    }




    public void calculateTurretPIDPower(double posX, double posY, double robotHeading, double turretHeading){
        //put logic for the PD controller in here

        double deltaTime = timer.seconds();
        timer.reset();

        /*
            need this so it is not undefined when called in the dTerm
        if(){
            turretPinionMotor.setPower(0);
            lastError = 0;
            return;
        }
        */


        //Math.atan2() should be the target heading; robotHeading is the current heading


        double disX = turretTargetX - posX;
        double disY = turretTargetY - posY;


        //replace apriltag bearing with calculated position bearing

        //need to convert Math.atan2 to degrees; make sure turretHeading and robotHeading are in degrees
        double error = Math.toDegrees(Math.atan2(disY, disX)) - robotHeading - turretHeading;



        double pTerm = error * kP;

        double dTerm = 0;

        if(deltaTime > 0){

            dTerm = (error - lastError)/deltaTime * kD;
        }

        //adjust the headingOffsetTolerance
        double headingOffsetTolerance = 0.4;
        if(Math.abs(error) < headingOffsetTolerance){
            power = 0;

        } else {
            //tune this
            //tune the MAX_POWER
            double MAX_POWER = 0.5;
            double MIN_POWER = -0.5;
            power = Range.clip(pTerm + dTerm, MIN_POWER, MAX_POWER);
        }


        turretPinionMotor.setPower(power);
        lastError = error;

    }


    //find the values for the turntable and pinion
    private final double turntableGearTeeth = 155;
    private final double pinionGearTeeth = 45;
    final double turretGearRatio = turntableGearTeeth/pinionGearTeeth;

    public void slipRing() {
        //finish
        // set an internal gear ratio
        double turretCurrentPos = (turretPinionMotor.getCurrentPosition() / turretGearRatio);
        double turretDegreePos = turretCurrentPos / 360;


        int lowLimit = 180;
        int highLimit = -180;
        //check if this goes in the proper directions
        if((turretDegreePos < lowLimit) || (turretDegreePos > highLimit)) {
            power = -(0.45 * power);
        }


    }






    //get and set

    public void setTurretPD(double newkP, double newKD){
        kP = newkP;
        kD = newKD;
    }

    public void resetTimer(){
        timer.reset();
    }






}
