package teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;

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


    //GET RID OF APRILTAG
    public void calculateTurretPIDPower(AprilTagClusterDetection clustID){
        //put logic for the PD controller in here

        double deltaTime = timer.seconds();
        timer.reset();

        if(clustID == null){
            turretPinionMotor.setPower(0);
            lastError = 0;
            return;
        }


        double goalX = 0;
        double error = goalX - clustID.ftcPose.bearing;
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


        int lowLimit = 0;
        int highLimit = 0;
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
