package org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAINearestAttackableTarget.class)
public interface AccessorEntityAINearestAttackableTarget {
    @Accessor
    EntityLivingBase getTargetEntity();
}
