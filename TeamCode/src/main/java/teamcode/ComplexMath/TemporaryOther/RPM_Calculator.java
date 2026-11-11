package teamcode.ComplexMath.TemporaryOther;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.robotcore.hardware.DcMotorEx;

public class RPM_Calculator {

    DcMotorEx flywheelMotor;
    public RPM_Calculator() {

        flywheelMotor = hardwareMap.get(DcMotorEx.class, "leftShooterMotor");



    }



    public double IdealRPMRegression(){
        //put rpm regression formula here
        return 0;

    }




}
