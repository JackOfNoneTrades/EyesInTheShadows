package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

public class FlyingTargetTamedWolves extends FlyingAINearestAttackableTarget {

    EntityEyes eyes;

    public FlyingTargetTamedWolves(EntityFlying entity, Class<? extends Entity> targetClass, int targetChance, boolean shouldCheckSight) {
        super(entity, targetClass, targetChance, shouldCheckSight);
        this.eyes = (EntityEyes) entity;
    }

    @Override
    public boolean shouldExecute() {
        if (!Config.eyesAttackTamedWolves || eyes.getBrightness() <= 0) {
            return false;
        }
        boolean superShouldExecute = super.shouldExecute();
        EntityLivingBase entityLivingBase = this.getTargetEntity();

        if (!(entityLivingBase instanceof EntityWolf)) {
            return false;
        }
        if (!((EntityWolf) entityLivingBase).isTamed()) {
            return false;
        }
        return superShouldExecute;
    }
}
