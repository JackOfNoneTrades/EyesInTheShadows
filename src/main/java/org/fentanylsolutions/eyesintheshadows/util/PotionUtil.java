package org.fentanylsolutions.eyesintheshadows.util;

import net.minecraft.potion.Potion;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

public class PotionUtil {

    public static Potion getPotionById(int id) {
        Potion res = null;
        for (Potion p : Potion.potionTypes) {
            if (p != null && p.getId() == id) {
                res = p;
                EyesInTheShadows.debug("Found potion with id " + id + "! (" + p.getName() + ")");
            }
        }
        return res;
    }

    public static Potion getPotionByName(String name) {
        Potion res = null;
        for (Potion p : Potion.potionTypes) {
            if (p != null && p.getName()
                .equals(name)) {
                res = p;
                EyesInTheShadows.debug("Found potion with id " + name + "! (" + p.getName() + ")");
            }
        }
        return res;
    }

    public static void printPotionNames() {
        EyesInTheShadows.LOG.info("=========Potion List=========");
        EyesInTheShadows.LOG.info(
            "The printing of this list is for you to know which enchantment has which id. You can disable this print in the configs.");
        for (Potion p : Potion.potionTypes) {
            if (p != null) {
                EyesInTheShadows.LOG.info(p.getId() + ": " + p.getName());
            }
        }
        EyesInTheShadows.LOG.info("=============================");
    }
}
