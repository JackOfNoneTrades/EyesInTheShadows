package org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft;

import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAIAttackOnCollide.class)
public interface AccessorEntityAIAttackOnCollide {
    @Accessor
    double getSpeedTowardsTarget();

    @Accessor
    public void setSpeedTowardsTarget(double speedTowardsTarget);
}
