package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

public class EntityAINearestAttackableTargetDormant extends EntityAINearestAttackableTarget {

    EntityCreature entityCreature;

    public EntityAINearestAttackableTargetDormant(EntityCreature entityCreature, int p_i1663_3_, boolean p_i1663_4_) {
        super(entityCreature, EntityEyes.class, p_i1663_3_, p_i1663_4_);
        this.entityCreature = entityCreature;
    }

    @Override
    public boolean shouldExecute() {
        if (true) {
            return super.shouldExecute();
        }
        if (Config.mobsAttackDormantEyes) {
            return super.shouldExecute();
        }
        EntityEyes eyes;
        if (this.entityCreature.getAttackTarget() instanceof EntityEyes) {
            eyes = (EntityEyes) this.entityCreature.getAttackTarget();
        } else {
            return false;
        }
        if (eyes.getBrightness() <= 0) {
            return false;
        }

        return super.shouldExecute();
    }

    @Override
    public boolean continueExecuting() {
        if (true) {
            return super.continueExecuting();
        }
        if (Config.mobsAttackDormantEyes) {
            return super.continueExecuting();
        }
        EntityEyes eyes;
        if (this.entityCreature.getAttackTarget() instanceof EntityEyes) {
            eyes = (EntityEyes) this.entityCreature.getAttackTarget();
        } else {
            return false;
        }
        if (eyes.getBrightness() <= 0) {
            return false;
        }

        return super.continueExecuting();
    }
}
