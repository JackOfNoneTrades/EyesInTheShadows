package org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {

    private EntityLivingBase eyesInTheShadows$self() {
        return (EntityLivingBase) (Object) this;
    }

    @Inject(method = "attackEntityAsMob", at = @At("TAIL"), require = 1, cancellable = true)
    private void eyesInTheShadows$attackEntityAsMob(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof EntityEyes && !cir.getReturnValueZ()
            && EyesInTheShadows.varInstanceCommon.entitiesAttackingEyesList
                .contains(eyesInTheShadows$self().getClass())) {
            entity.attackEntityFrom(DamageSource.causeMobDamage(eyesInTheShadows$self()), 1);
            cir.setReturnValue(true);
        }
    }
}
