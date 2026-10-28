package teamcode.ComplexMath.Storage;

public class PoseStorage {


    //volatile saves across threads, static keeps it constant across all classes
    private static volatile double PosX = 0;
    private static volatile double PosY = 0;
    private static volatile double robotHeading = 0;




    public void savePoseToStorage(double updatedX, double updatedY, double updatedHeading){
        PosX = updatedX;
        PosY = updatedY;
        robotHeading = updatedHeading;
    }

    public double getPosX(){
        return PosX;
    }

    public double getPosY(){
        return PosY;

    }

    public double getRobotHeading(){
        return robotHeading;

    }










}
