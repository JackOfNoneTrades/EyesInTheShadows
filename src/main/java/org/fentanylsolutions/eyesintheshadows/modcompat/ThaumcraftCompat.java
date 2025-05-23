package org.fentanylsolutions.eyesintheshadows.modcompat;

import net.minecraft.entity.player.EntityPlayerMP;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;

public class ThaumcraftCompat {

    // TODO: configurable
    public static void registerAspects() {
        ThaumcraftApi.registerEntityTag(
            "eyesintheshadows.Eyes",
            (new AspectList()).add(Aspect.DARKNESS, 5)
                .add(Aspect.ELDRITCH, 2));
    }

    public static void addStickyWarp(EntityPlayerMP player, int amount) {
        Thaumcraft.addStickyWarpToPlayer(player, amount);
    }

    public static void addWarp(EntityPlayerMP player, int amount, boolean temporary) {
        Thaumcraft.addWarpToPlayer(player, amount, temporary);
    }
}
