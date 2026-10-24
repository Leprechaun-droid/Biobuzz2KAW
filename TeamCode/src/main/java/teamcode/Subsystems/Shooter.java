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
        //g is not negative because the negative was put outside of variable value
        private final double g = 9.80665;
        //fill in with actual height relative to camera unless I find a way to do it from shooter
        //believe I put a heightThreshold variable in the vision class. can use that to define h.

        //
        // plugging in 43 for now. It is the number I measured from the ground to a midpoint between
        // the displacement of the top and bottom cell; need to measure the distance from the camera to the ground and subtract it from this
        // because the camera measures the apriltag's height from the camera, not the ground
        private final double h = 43;
        //fill in with actual diameter

        private final double flywheelDiameter = 0;
        //FIND the motor ticks per revolution; find motor first. ask design team what it is
        double TICKS_PER_SHOOTER_MOTOR_REV = 0;

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
            double encoderPPR = 28;
            double encoderCPM = encoderPPR * 4 * gearRatio;
            return (((getTicksPerSec()/ encoderCPM) * 60) / gearRatio);

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







    //THIS IS IRRELEVANT FOR THE FIRST COMPETITION. THIS IS A WORK IN PROGRESS.

    //make method for calculating distance from cell
    //I'm assuming we're using a dual flywheel, the kind with a wheel on both sides of the ball

    /*
    private double calcOptimalTargetRPM(double disFromCell, double heightThreshold){
        //Finding Target Velocity
        double d = disFromCell;
        double h = heightThreshold;
        double initVel = (d / (Math.cos(shooterAngle))) * Math.sqrt(g/(2*(Math.tan(shooterAngle)*d - h)));

        //change to wheelVel = 2initVel if single flywheel or the non-mirrored dual flywheel
        // may change this
        double wheelVel = 2 * initVel;
        double D = flywheelDiameter;
        double initRPM = (wheelVel * 60)/(Math.PI * D);
        double targetRPM = initRPM * kRPM;

        return targetRPM;
    }

    public void advancedShoot(){
        //fill arguments with disFromCell and heightThreshold
        //double targetRPM = calcOptimalTargetRPM(robot.vision.findCellDis(), h);
        //double targetVelocity = (targetRPM * TICKS_PER_SHOOTER_MOTOR_REV) / 60;
        //double check this line with the addition of the dual motors
        double currentVelocity = shooterMotor.getVelocity();
        //replace with actual own PID class; get rid of FTCLib
        //double power = shooterPID.calculate(currentVelocity, targetVelocity) * kF;
        //shooterMotor.setPower(power);

    }

    */









}
