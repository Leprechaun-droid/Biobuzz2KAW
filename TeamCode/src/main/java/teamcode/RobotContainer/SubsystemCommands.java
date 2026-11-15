package teamcode.RobotContainer;

import static com.pedropathing.ivy.commands.Commands.infinite;
import static com.pedropathing.ivy.commands.Commands.instant;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class SubsystemCommands {


    Robot robot;

    public SubsystemCommands(HardwareMap hardwareMap, Robot robot){
        this.robot = robot;




    }















    public Command activateMatchLoader(){

        return instant(() -> robot.matchLoader.lowerMatchLoader());
    }

    public Command restMatchLoader(){
        return instant(() -> robot.matchLoader.raiseMatchLoader());
    }




    public Command openGate(){
        return instant(() -> robot.turretGate.moveGateToOpenPos());
    }

    public Command closeGate(){
        return instant(() -> robot.turretGate.moveGateToClosePos());
    }




    public Command updateShooterRPM(){
        // add the autoRPM algorithm in the setBaseTargetRPM algorithm
        return infinite(() -> robot.shooter.setBaseTargetRPM(2240));
    }
    public Command zeroShooterRPM(){
        return instant(() -> robot.shooter.setBaseTargetRPM(0));
    }

    public Command updateShooterPID(){
        return infinite(() -> robot.shooter.calculateShooterPID());
    }



    public Command fullIntakeCommand(){

        return instant(() -> robot.intake.fullIntake());
    }
    public Command zeroIntakeCommand(){
        return instant(() -> robot.intake.zeroIntake());
    }








}
