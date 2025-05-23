package org.fentanylsolutions.eyesintheshadows.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class TraceUtil {

    public static boolean canEntityBeSeenIgnoreWithoutBoundingBox(Entity player, Entity entity) {
        Vec3 eyePos = Vec3.createVectorHelper(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
        Vec3 targetPos = Vec3
            .createVectorHelper(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);

        return entity.worldObj.func_147447_a(eyePos, targetPos, false, true, false) == null;
    }
}
