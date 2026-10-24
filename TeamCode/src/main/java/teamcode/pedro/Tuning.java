package teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import teamcode.pedro.procedures.ForesightTuner;
import teamcode.pedro.procedures.MecanumTuner;
import teamcode.pedro.procedures.PinpointTuner;
import teamcode.pedro.procedures.Tests;

public class Tuning {
    // Tuners go here

    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }



    @Tuner
    public static Procedure tests(){
        return new Tests(hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig), hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig), () -> new Foresight(Constants.foresightConfig));
    }


    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
     }


    //need to run and get pinpointTuner localizer Config before running Foresight tuner
    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner((hardwareMap) -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig), (hardwareMap) -> new Mecanum(hardwareMap, Constants.drivetrainConfig));
    }







}
