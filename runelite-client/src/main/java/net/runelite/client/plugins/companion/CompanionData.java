package net.runelite.client.plugins.companion;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

@Getter
public enum CompanionData {
    // Example transmog option
//    NIEVE("Nieve", Lists.newArrayList(392, 27644, 27640, 19951, 3661, 28827, 9644, 27654, 9640, 11048), 813, 1205, 30,1,1205, 64, -1, -1,null),
//    KONAR("Konar", Lists.newArrayList(36162), 8219, 8218, 60, 1, -1, 108, -1, -1, null);
    CUSTOM("Custom Model", Lists.newArrayList(-1), -1, -1, -1, -1,-1),
    NIEVE("Nieve", Lists.newArrayList(392, 27644, 27640, 19951, 3661, 28827, 9644, 27654, 9640, 11048), 813, 1205, 60, 120,  1),
    KONAR("Konar", Lists.newArrayList(36162), 8219, 8218, 60, 60,1),
    Whisperer("Whisperer", Lists.newArrayList(49222,49218,49221,49224,49219), 10230,10232, 60, 120, 1),
    WiseOldMan("Wise Old Man", Lists.newArrayList(187,9103,4925,315,173,10218,3711,279,8919), 813,1146, 60, 120, 1),
    Zilyana("Zilyana", Lists.newArrayList(187,9103,4925,315,173,10218,3711,279,8919), 6966,6965, 60, 120, 1),
    Guthan("Guthan", Lists.newArrayList(6654,6673,6642,6666,6679,6710), 1205,813, 30, 120,1),
    Nightmare("Nightmare", Lists.newArrayList(39196), 8593,8634,60,60, 2),
    GnomeChild("Gnome Child", Lists.newArrayList(2909,2899,2918), 195,189, 60, 120, 1);


    // Getters for each property
    final String name;
    final List<Integer> modelIDs;
    final int standingAnim;
    final int walkAnim;
    final int walkingSpeed;
    final int poseSpeed;
    final int size;
//    final int runAnim;
//    final int scale;
//    final int ambient;
//    final int contrast;
//    final List<Short> recolorIDs;

    CompanionData(String name, List<Integer> modelIDs, int standingAnim, int walkAnim, int walkingSpeed, int poseSpeed, int size) {
        this.name = name;
        this.modelIDs = modelIDs;
        this.standingAnim = standingAnim;
        this.walkAnim = walkAnim;
        this.walkingSpeed = walkingSpeed;
        this.poseSpeed = poseSpeed;
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



