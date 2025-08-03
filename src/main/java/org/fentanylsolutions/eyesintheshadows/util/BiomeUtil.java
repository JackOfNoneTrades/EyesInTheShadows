package org.fentanylsolutions.eyesintheshadows.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

public class BiomeUtil {

    public static List<BiomeGenBase> getBiomeList() {
        List<BiomeGenBase> res = new ArrayList<>();

        Arrays.stream(BiomeGenBase.getBiomeGenArray())
            .filter(Objects::nonNull)
            .forEach(res::add);

        return (res);
    }

    public static void printBiomeNames() {
        EyesInTheShadows.LOG.info("=========Biome List=========");
        for (BiomeGenBase b : getBiomeList()) {
            EyesInTheShadows.LOG.info("{} ({})", b.biomeName, b.biomeID);
        }
        EyesInTheShadows.LOG.info("=============================");
    }
}
