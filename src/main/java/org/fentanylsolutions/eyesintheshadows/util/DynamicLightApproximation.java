package org.fentanylsolutions.eyesintheshadows.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Shared dynamic-light approximation used on both client and server.
 *
 * Porting notes:
 * This class intentionally mirrors Angelica's dynamic light heuristics from
 * {@code com.gtnewhorizons.angelica.dynamiclights.DynamicLights}:
 * - {@code getLuminanceFromEntity}
 * - {@code getLuminanceFromItemStack}
 * - light falloff/radius logic used by {@code maxDynamicLightLevel}
 *
 * Keep all luminance rules centralized here so updates for new Angelica versions
 * are a straightforward diff against those methods.
 */
public final class DynamicLightApproximation {

    // Matches Angelica DynamicLights.MAX_RADIUS
    private static final double LIGHT_RADIUS = 7.75D;
    private static final double LIGHT_RADIUS_SQ = LIGHT_RADIUS * LIGHT_RADIUS;

    private DynamicLightApproximation() {}

    public static float getBrightness(World world, int x, int y, int z) {
        if (world == null) {
            return 0F;
        }

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            x - LIGHT_RADIUS,
            y - LIGHT_RADIUS,
            z - LIGHT_RADIUS,
            x + LIGHT_RADIUS,
            y + LIGHT_RADIUS,
            z + LIGHT_RADIUS);

        double lightLevel = 0.0D;

        List<EntityLivingBase> livingEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        for (EntityLivingBase living : livingEntities) {
            lightLevel = maxDynamicLightLevel(x, y, z, living, lightLevel);
            if (lightLevel >= 15.0D) {
                return 1.0F;
            }
        }

        List<EntityItem> itemEntities = world.getEntitiesWithinAABB(EntityItem.class, searchBox);
        for (EntityItem itemEntity : itemEntities) {
            lightLevel = maxDynamicLightLevel(x, y, z, itemEntity, lightLevel);
            if (lightLevel >= 15.0D) {
                return 1.0F;
            }
        }

        return (float) MathHelper.clamp_double(lightLevel / 15.0D, 0.0D, 1.0D);
    }

    private static double maxDynamicLightLevel(int x, int y, int z, Entity source, double currentLightLevel) {
        int luminance = getLuminanceFromEntity(source);
        if (luminance <= 0) {
            return currentLightLevel;
        }

        // Keep coordinate math aligned with Angelica's int-position variant.
        double dx = x - source.posX + 0.5D;
        double dy = y - (source.posY + source.getEyeHeight()) + 0.5D;
        double dz = z - source.posZ + 0.5D;
        double distanceSquared = dx * dx + dy * dy + dz * dz;

        if (distanceSquared > LIGHT_RADIUS_SQ) {
            return currentLightLevel;
        }

        double multiplier = 1.0D - Math.sqrt(distanceSquared) / LIGHT_RADIUS;
        double lightLevel = multiplier * (double) luminance;
        return Math.max(currentLightLevel, lightLevel);
    }

    public static int getLuminanceFromEntity(Entity source) {
        if (source == null) {
            return 0;
        }

        if (source.isBurning()) {
            return 15;
        }

        if (source.isInsideOfMaterial(Material.water)) {
            return 0;
        }

        if (source instanceof EntityItem) {
            return getLuminanceFromItemStack(((EntityItem) source).getEntityItem());
        }

        if (source instanceof EntityLivingBase) {
            int luminance = 0;
            EntityLivingBase living = (EntityLivingBase) source;

            // Matches Angelica's base equipment scan (hand + armor slots).
            for (int i = 0; i < 5; i++) {
                ItemStack stack = living.getEquipmentInSlot(i);
                if (stack != null) {
                    luminance = Math.max(luminance, getLuminanceFromItemStack(stack));
                }
            }
            return luminance;
        }

        return 0;
    }

    public static int getLuminanceFromItemStack(ItemStack stack) {
        if (stack == null) {
            return 0;
        }

        Item item = stack.getItem();
        if (item == null) {
            return 0;
        }

        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).field_150939_a;
            if (block != null) {
                return block.getLightValue();
            }
        }

        // Matches Angelica special-case
        if (item == Items.lava_bucket) {
            return Blocks.lava.getLightValue();
        }

        return 0;
    }
}
