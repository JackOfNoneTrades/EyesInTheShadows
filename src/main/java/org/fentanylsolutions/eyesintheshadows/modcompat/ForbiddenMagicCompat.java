package org.fentanylsolutions.eyesintheshadows.modcompat;

import fox.spiteful.forbidden.DarkAspects;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ForbiddenMagicCompat {

    // TODO: configurable
    public static void registerAspects() {
        ThaumcraftApi.registerEntityTag(
            "eyesintheshadows.Eyes",
            (new AspectList()).add(Aspect.DARKNESS, 5)
                .add(DarkAspects.ENVY, 2));
    }
}
