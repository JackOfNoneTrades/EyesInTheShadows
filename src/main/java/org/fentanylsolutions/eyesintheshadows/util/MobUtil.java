package org.fentanylsolutions.eyesintheshadows.util;

import net.minecraft.entity.EntityList;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

public class MobUtil {

    public static void test() {
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            String name = (String) e;
            System.out.println(name);
            // System.out.println(EntityList.stringToClassMapping.get(name));
            // System.out.println(EntityList.classToStringMapping.get(e));
        }
    }

    public static void printMobNames() {
        EyesInTheShadows.LOG.info("=========Mob List=========");
        EyesInTheShadows.LOG.info(
            "The printing of this list is for you to know which mob has which name. You can disable this print in the configs.");
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            System.out.println(e + " (" + EntityList.stringToClassMapping.get(e) + ")");
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
