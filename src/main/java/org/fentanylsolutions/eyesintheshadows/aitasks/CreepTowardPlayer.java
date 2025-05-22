package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft.AccessorEntityAIAttackOnCollide;

public class CreepTowardPlayer extends EntityAIAttackOnCollide {

    private EntityEyes eyes;

    public CreepTowardPlayer(EntityCreature creature, double p_i1636_2_, boolean p_i1636_4_) {
        super(creature, p_i1636_2_, p_i1636_4_);
        // eyes = (EntityEyes) creature;
    }

    @Override
    public boolean continueExecuting() {
        if (eyes.isPlayerLookingInMyGeneralDirection() || eyes.getBrightness() <= 0) {
            return false;
        }
        return super.continueExecuting();
    }

    @Override
    public boolean shouldExecute() {
        if (eyes.isPlayerLookingInMyGeneralDirection() || eyes.getBrightness() <= 0) {
            return false;
        }
        return super.shouldExecute();
    }

    @Override
    public void updateTask() {
        double speed = eyes.getSpeedFromAggro();
        // EyesInTheShadows.debug("aggro: " + eyes.getAggroLevel() + ", speed: " + speed);
        ((AccessorEntityAIAttackOnCollide)this).setSpeedTowardsTarget(speed);
        super.updateTask();
    }
}
