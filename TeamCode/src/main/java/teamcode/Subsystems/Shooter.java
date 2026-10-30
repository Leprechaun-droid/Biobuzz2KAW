package teamcode.Subsystems;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;



public class Shooter {

    //CONSTANTS
        //put in actual shooterAngle when designers tells me
        private final double shooterAngle = 25;


        //fill in with actual height relative to camera unless I find a way to do it from shooter
        //believe I put a heightThreshold variable in the vision class. can use that to define h.

        //
        // plugging in 43 for now. It is the number I measured from the ground to a midpoint between
        // the displacement of the top and bottom cell; need to measure the distance from the camera to the ground and subtract it from this
        // because the camera measures the apriltag's height from the camera, not the ground
        private final double h = 43;

    //PID Tuning Values

        public static double kV = 0.0008;
        public static double kS = 0.07;
        public static double kP = 0.005;

    //Objects
        //put tuning constants in class parameters when I make the PIDController class
        private DcMotorEx shooterMotor;
        //private DcMotorEx rightShooterMotor;
        public int baseTargetRPM = 0; // Initialize to 0 so it doesn't spin immediately
    public Shooter(HardwareMap hardwareMap) {
            shooterMotor = hardwareMap.get(DcMotorEx.class, "leftShooterMotor");

            shooterMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

            shooterMotor.setDirection(DcMotorSimple.Direction.FORWARD);

            shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

    }




        public double getTicksPerSec(){
            //Check if this is the right Vel
            return shooterMotor.getVelocity();

        }

        public void setShooterMotorPowers(double power){
            shooterMotor.setPower(power);
        }

        public void emergencyShooterStop(){
            shooterMotor.setPower(0);

        }


        public double getRPM(){
        //FIND ENCODER CPM; look in brogan pratt video for encoderCPM definition (should already be open); flywheel is 6000RPM motor
            double gearRatio = 1;
            //find encoderCPM (encoder counts per motor revolution)
            double ticksPerRev = 28;

            //GearRatio is redundant, but I am leaving it here for future reference
            double TICKS_PER_FLYWHEEL_REVOLUTION = ticksPerRev * gearRatio;
            return (getTicksPerSec()/ TICKS_PER_FLYWHEEL_REVOLUTION) * 60;

        }

        public void calculateShooterPID(){

            double feedForward = (kV * baseTargetRPM) + kS;
            double error = baseTargetRPM - getRPM();
            double feedBack = error * kP;
            if (baseTargetRPM == 0) feedForward = 0; // Don't apply kS if we aren't trying to spin
            double power = feedForward + feedBack;


            setShooterMotorPowers(power);

        }



    // --------------
    // Basic Shooting
    // --------------


    public void setBaseTargetRPM(int targRPMBase){
        baseTargetRPM = targRPMBase;
    }









    //Auto Commands
    public Command basicShoot(){return Commands.instant(() -> setBaseTargetRPM(1000));}
    public Command zeroShoot(){return Commands.instant(() -> setBaseTargetRPM(0));}









    public int getTargetRPM(){
        return baseTargetRPM;

    }













}
