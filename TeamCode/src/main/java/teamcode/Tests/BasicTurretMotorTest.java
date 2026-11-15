package teamcode.Tests;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp
public class BasicTurretMotorTest extends LinearOpMode {





    public void runOpMode(){

        DcMotorEx testMotor = hardwareMap.get(DcMotorEx.class, "turretPinion");



        testMotor.setDirection(REVERSE);
        testMotor.setMode(STOP_AND_RESET_ENCODER);
        testMotor.setMode(RUN_WITHOUT_ENCODER);



        waitForStart();

        while(opModeIsActive()){

            testMotor.setPower(gamepad1.left_stick_y/4);


            telemetry.addData("TurretPos", testMotor.getCurrentPosition());
            telemetry.addData("TurretVel", testMotor.getVelocity());

            telemetry.update();




        }


    }






}
