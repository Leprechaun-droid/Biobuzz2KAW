package teamcode.OpModes.Auto.Solo;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class BlueGardenCornerSoloAuto extends LinearOpMode {


    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose startRedGarden = poseFactory.of(133.8808, 107.1101, 180);
    private final Pose startToFirstShoot = poseFactory.of(91, 126, 265);
    private final Pose firstShootToGardenStart = poseFactory.of(91, 126, 0);
    private final Pose firstShootToGarden = poseFactory.of(133.3849, 134.1465, 0);
    private final Pose firstShootToGardenControl1 = poseFactory.of(66, 141, 0);
    private final Pose gardenToParkShootStart = poseFactory.of(133.3849, 134.1465, 0);
    private final Pose gardenToParkShoot = poseFactory.of(73.9035, 17.5103, 0);
    private final Pose gardenToParkShootControl1 = poseFactory.of(73.0562, 141.5, 0);
    private final Pose parkShootToGardenFlower = poseFactory.of(131.6982, 97.8947, 340);
    private final Pose parkShootToGardenFlowerControl1 = poseFactory.of(70.0464, 139.0067, 0);
    private final Pose gardenFlowerToFlowerShootStart = poseFactory.of(131.6982, 97.8947, 0);
    private final Pose gardenFlowerToFlowerShoot = poseFactory.of(90.572, 119.3808, 260);
    private final Pose point6Start = poseFactory.of(90.572, 119.3808, 180);
    private final Pose point6 = poseFactory.of(132.6193, 50.4211, 180);
    private final Pose point7 = poseFactory.of(137.8222, 134.4438, 270);






























    @Override
    public void runOpMode(){





    }





}
