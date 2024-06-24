package net.runelite.client.plugins.followerTransmog;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

@Getter
public enum TransmogData {
    // Example transmog option
//    NIEVE("Nieve", Lists.newArrayList(392, 27644, 27640, 19951, 3661, 28827, 9644, 27654, 9640, 11048), 813, 1205, 30,1,1205, 64, -1, -1,null),
//    KONAR("Konar", Lists.newArrayList(36162), 8219, 8218, 60, 1, -1, 108, -1, -1, null);
    CUSTOM("Custom Model", Lists.newArrayList(-1), -1, -1, -1,-1),
    NIEVE("Nieve", Lists.newArrayList(392, 27644, 27640, 19951, 3661, 28827, 9644, 27654, 9640, 11048), 813, 1205, 45,1),
    KONAR("Konar", Lists.newArrayList(36162), 8219, 8218, 60,1);
    // Getters for each property
    final String name;
    final List<Integer> modelIDs;
    final int standingAnim;
    final int walkAnim;
    final int speed;
    final int size;
//    final int runAnim;
//    final int scale;
//    final int ambient;
//    final int contrast;
//    final List<Short> recolorIDs;

    TransmogData(String name, List<Integer> modelIDs, int standingAnim, int walkAnim, int speed, int size) {
        this.name = name;
        this.modelIDs = modelIDs;
        this.standingAnim = standingAnim;
        this.walkAnim = walkAnim;
        this.speed = speed;
        this.size = size;

//        this.runAnim = runAnim;
//        this.scale = scale;
//        this.ambient = ambient;
//        this.contrast = contrast;
//        this.recolorIDs = recolorIDs;
    }


    //    public int getRunAnim() {
//        return runAnim;
//    }

//    public int getScale() {
//        return scale;
//    }

//    public int getAmbient() {return ambient;}

//    public int getContrast() {
//        return contrast;
//    }

//    public List<Short> getRecolorIDs() {
//        return recolorIDs;
//    }
}



