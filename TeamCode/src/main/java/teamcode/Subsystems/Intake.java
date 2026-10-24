package teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Intake {



    DcMotor intakeMotor;
    DcMotor transferMotor;


    //find how to pair variables to gamepads in constructor
    public Intake(HardwareMap hardwareMap){
        //adjust deviceName to config if I give it a different name in the configuration

        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        transferMotor = hardwareMap.get(DcMotor.class, "transferMotor");

        intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        transferMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        transferMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        transferMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);



    }








    public void justFrontIntake(){

        intakeMotor.setPower(1);
    }

    public void justFrontEject(){
            intakeMotor.setPower(-1);

    }

    public void justTransIntake(){
        transferMotor.setPower(1);
    }
    public void justTransEject(){ transferMotor.setPower(-1);}

    public void fullIntake(){
                //adjust directions of the setPower as fit
                intakeMotor.setPower(1);
                transferMotor.setPower(1);
    }

    public void fullEject(){
            intakeMotor.setPower(-1);
            transferMotor.setPower(-1);

    }

    public void zeroTransfer(){
        transferMotor.setPower(0);
    }

    public void zeroIntake(){
        intakeMotor.setPower(0);
    }











}
