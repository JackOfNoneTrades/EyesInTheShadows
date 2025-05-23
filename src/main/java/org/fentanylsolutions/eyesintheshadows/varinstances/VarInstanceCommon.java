package org.fentanylsolutions.eyesintheshadows.varinstances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft.AccessorDimensionManager;
import org.fentanylsolutions.eyesintheshadows.util.*;

import cpw.mods.fml.common.Loader;

public class VarInstanceCommon {

    /* A faster random. http://demesos.blogspot.com/2011/09/pseudo-random-number-generators.html */
    public XSTR rand = new XSTR();
    public String disappearSound = EyesInTheShadows.MODID + ":" + "mob.eyes.disappear";
    public String laughSound = EyesInTheShadows.MODID + ":" + "mob.eyes.laugh";
    public String jumpScareSound = EyesInTheShadows.MODID + ":" + "mob.eyes.jumpscare";

    public HashMap<String, Potion> potionList;
    public HashMap<String, Potion> potionCollisionList;
    public HashMap<String, Potion> potionLookList;
    public HashMap<String, DimensionUtil.SimpleDimensionObj> dimensionList;
    public ArrayList<Class> entitiesAttackingEyesList;
    public ArrayList<Class> entitiesAttackedByEyesList;
    public ArrayList<Class> entitiesFleeingEyesList;
    public ArrayList<Class> entitiesThatEyesFleeList;

    public Hashtable<Integer, Class<? extends WorldProvider>> providers;

    public boolean witcheryLoaded;
    public int daysUntilHalloween = TimeUtil.getDaysUntilNextHalloween();

    public VarInstanceCommon() {}

    public void postInitHook() {
        witcheryLoaded = Loader.isModLoaded("witchery");
        providers = AccessorDimensionManager.getProviders();
        buildPotionList();
        buildDimensionList();
        buildMobLists();
    }

    public void buildPotionList() {
        potionList = new HashMap<>();
        for (String s : Config.potionNames) {
            Potion p = PotionUtil.getPotionByName(s);
            if (p == null) {
                EyesInTheShadows.LOG.error("Failed to get potion for name " + s);
            } else {
                potionList.put(p.getName(), p);
            }
        }
        potionCollisionList = new HashMap<>();
        for (String s : Config.potionCollisionNames) {
            Potion p = PotionUtil.getPotionByName(s);
            if (p == null) {
                EyesInTheShadows.LOG.error("Failed to get potion for name " + s);
            } else {
                potionCollisionList.put(p.getName(), p);
            }
        }
        potionLookList = new HashMap<>();
        for (String s : Config.potionLookNames) {
            Potion p = PotionUtil.getPotionByName(s);
            if (p == null) {
                EyesInTheShadows.LOG.error("Failed to get potion for name " + s);
            } else {
                potionLookList.put(p.getName(), p);
            }
        }
    }

    public void buildBiomeList() {
        dimensionList = new HashMap<>();
        for (String s : Config.dimensionSpawnNames) {
            DimensionUtil.SimpleDimensionObj sdo = DimensionUtil.getSimpleDimensionObj(s);
            if (sdo == null) {
                EyesInTheShadows.LOG.error("Failed to get dimension for name " + s);
            } else {
                dimensionList.put(sdo.getName(), sdo);
            }
        }
    }

    public void buildDimensionList() {
        dimensionList = new HashMap<>();
        for (String s : Config.dimensionSpawnNames) {
            DimensionUtil.SimpleDimensionObj sdo = DimensionUtil.getSimpleDimensionObj(s);
            if (sdo == null) {
                EyesInTheShadows.LOG.error("Failed to get dimension for name " + s);
            } else {
                dimensionList.put(sdo.getName(), sdo);
            }
        }
    }

    public void buildMobLists() {
        entitiesAttackingEyesList = new ArrayList<>();
        for (String s : Config.mobStringsAttackingEyes) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                EyesInTheShadows.LOG.error("Failed to get mob class for name " + s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    entitiesAttackingEyesList.add(c);
                } catch (ClassNotFoundException e) {
                    EyesInTheShadows.LOG.error("Failed to get class for classname " + class_);
                }
            }
        }

        entitiesAttackedByEyesList = new ArrayList<>();
        for (String s : Config.mobStringsThatEyesAttack) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                EyesInTheShadows.LOG.error("Failed to get mob class for name " + s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    entitiesAttackedByEyesList.add(c);
                } catch (ClassNotFoundException e) {
                    EyesInTheShadows.LOG.error("Failed to get class for classname " + class_);
                }
            }
        }

        entitiesFleeingEyesList = new ArrayList<>();
        for (String s : Config.mobStringsFleeingEyes) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                EyesInTheShadows.LOG.error("Failed to get mob class for name " + s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    entitiesFleeingEyesList.add(c);
                } catch (ClassNotFoundException e) {
                    EyesInTheShadows.LOG.error("Failed to get class for classname " + class_);
                }
            }
        }

        entitiesThatEyesFleeList = new ArrayList<>();
        for (String s : Config.mobStringsThatEyesFlee) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                EyesInTheShadows.LOG.error("Failed to get mob class for name " + s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    entitiesThatEyesFleeList.add(c);
                } catch (ClassNotFoundException e) {
                    EyesInTheShadows.LOG.error("Failed to get class for classname " + class_);
                }
            }
        }
    }
}
