package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft.AccessorEntityAINearestAttackableTarget;

public class TargetEyesTamed extends EntityAITargetTamed {

    public TargetEyesTamed(EntityTameable entityTameable, Class class_, int p_i1666_3_, boolean p_i1666_4_) {
        super(entityTameable, class_, p_i1666_3_, p_i1666_4_);
    }

    @Override
    public boolean shouldExecute() {
        if (!Config.wolvesAttackEyes) {
            return false;
        }

        /* super.shouldExecute() must be called at the beginning because it sets the target entity field */
        boolean superShouldExecute = super.shouldExecute();
        EntityLivingBase entityLivingBase = ((AccessorEntityAINearestAttackableTarget) this).getTargetEntity();
        if (!(entityLivingBase instanceof EntityEyes)) {
            return false;
        }
        if (((EntityEyes) entityLivingBase).getBrightness() <= 0 && !Config.mobsAttackDormantEyes) {
            return false;
        }
        return superShouldExecute;
    }
}
