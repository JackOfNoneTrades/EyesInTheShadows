package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityWolf;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft.AccessorEntityAINearestAttackableTarget;

public class TargetTamedWolves extends EntityAINearestAttackableTarget {

    EntityEyes eyes;

    public TargetTamedWolves(EntityEyes eyes, Class p_i1663_2_, int p_i1663_3_, boolean p_i1663_4_) {
        super(eyes, p_i1663_2_, p_i1663_3_, p_i1663_4_);
        this.eyes = eyes;
    }

    @Override
    public boolean shouldExecute() {
        if (!Config.eyesAttackTamedWolves || eyes.getBrightness() <= 0) {
            return false;
        }
        boolean superShouldExecute = super.shouldExecute();
        EntityLivingBase entityLivingBase = ((AccessorEntityAINearestAttackableTarget)this).getTargetEntity();

        if (!(entityLivingBase instanceof EntityWolf)) {
            return false;
        }
        if (!((EntityWolf) entityLivingBase).isTamed()) {
            return false;
        }
        return superShouldExecute;
    }
}
