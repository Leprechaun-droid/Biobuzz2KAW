package teamcode.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import teamcode.RobotContainer.Robot;

@TeleOp
public class MotorConfigTest extends LinearOpMode {

    DcMotor lf_motor;
    DcMotor lb_motor;
    DcMotor rf_motor;
    DcMotor rb_motor;

    @Override
    public void runOpMode(){
        lf_motor = hardwareMap.get(DcMotor.class, "lf_motor");
        lb_motor = hardwareMap.get(DcMotor.class, "lb_motor");
        rf_motor = hardwareMap.get(DcMotor.class, "rf_motor");
        rb_motor = hardwareMap.get(DcMotor.class, "rb_motor");

        lf_motor.setDirection(DcMotorSimple.Direction.REVERSE);
        lb_motor.setDirection(DcMotorSimple.Direction.REVERSE);
        rf_motor.setDirection(DcMotorSimple.Direction.FORWARD);
        rb_motor.setDirection(DcMotorSimple.Direction.FORWARD);



        lf_motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lb_motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rf_motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rb_motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        lf_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lb_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rf_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rb_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();


        while(opModeIsActive()){
            if(gamepad1.dpadLeftWasPressed()){
                lf_motor.setPower(0.3);
                lb_motor.setPower(0);
                rf_motor.setPower(0);
                rb_motor.setPower(0);
            } else if(gamepad1.dpadRightWasPressed()){
                lf_motor.setPower(0);
                lb_motor.setPower(0.3);
                rf_motor.setPower(0);
                rb_motor.setPower(0);

            } else if(gamepad1.dpadUpWasPressed()){
                lf_motor.setPower(0);
                lb_motor.setPower(0);
                rf_motor.setPower(0.3);
                rb_motor.setPower(0);

            } else if(gamepad1.dpadDownWasPressed()){
                lf_motor.setPower(0);
                lb_motor.setPower(0);
                rf_motor.setPower(0);
                rb_motor.setPower(0.3);
            } else if(gamepad1.atRest()){
                lf_motor.setPower(0);
                lb_motor.setPower(0);
                rf_motor.setPower(0);
                rb_motor.setPower(0);
            }
        }

    }




}
