package org.fentanylsolutions.eyesintheshadows.modcompat;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ThaumcraftCompat {
    // TODO: configurable
    public static void registerAspects() {
        ThaumcraftApi.registerEntityTag("eyesintheshadows.Eyes", (new AspectList()).add(Aspect.DARKNESS, 5).add(Aspect.ELDRITCH, 2));
    }
}
