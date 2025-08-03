package org.fentanylsolutions.eyesintheshadows.util;

import net.minecraft.entity.EntityList;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

public class MobUtil {

    public static void printMobNames() {
        EyesInTheShadows.LOG.info("=========Mob List=========");
        EyesInTheShadows.LOG.info(
            "The printing of this list is for you to know which mob has which name. You can disable this print in the configs.");
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            EyesInTheShadows.LOG.info(e + " (" + EntityList.stringToClassMapping.get(e) + ")");
        }
        EyesInTheShadows.LOG.info("=============================");
    }

    public static String getClassByName(String name) {
        Object res = EntityList.stringToClassMapping.get(name);
        if (res != null) {

            return ((Class) res).getCanonicalName();
        }
        return null;
    }
}
