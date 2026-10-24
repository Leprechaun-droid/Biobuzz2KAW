package teamcode.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class FlywheelSpinTest extends LinearOpMode {

    private DcMotor flywheel;





    @Override
    public void runOpMode(){
        flywheel = hardwareMap.get(DcMotor.class, "shooterMotor");
        flywheel.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //might change later
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        double adjustablePower = 0;


        waitForStart();
        while(opModeIsActive()){

            if(gamepad1.left_trigger > 0.1 && gamepad1.right_trigger > 0.1) {
                if(gamepad1.yWasPressed()){
                    adjustablePower += 0.05;
                } else if (gamepad1.aWasPressed()){
                    adjustablePower -= 0.05;
                }
                flywheel.setPower(adjustablePower);
            } else if(gamepad1.b){
                flywheel.setPower(0);
            }


            telemetry.addData("Power", adjustablePower);
            telemetry.update();


        }

    }




}
