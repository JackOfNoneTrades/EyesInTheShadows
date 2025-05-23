package org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntitySenses;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLiving.class)
public interface AccessorEntityLiving {

    @Accessor
    public void setSenses(EntitySenses senses);
}
