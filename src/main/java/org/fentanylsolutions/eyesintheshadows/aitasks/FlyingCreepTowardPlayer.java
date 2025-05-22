package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import net.minecraft.entity.player.EntityPlayerMP;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

public class FlyingCreepTowardPlayer extends EntityAIBase {

    private final EntityEyes eyes;
    private EntityLivingBase target;
    private int courseChangeCooldown;
    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private int attackTick;

    // Attack range squared (for collision detection)
    private double attackRangeSq;

    public FlyingCreepTowardPlayer(EntityEyes creature) {
        this.eyes = creature;
        this.setMutexBits(3); // Same as EntityAIAttackOnCollide
        this.attackRangeSq = Math.pow(eyes.width * 2.0F * eyes.width * 2.0F, 2);
    }

    @Override
    public boolean shouldExecute() {
        target = this.eyes.getAttackTarget();

        if (target == null) {
            return false;
        } else if (!target.isEntityAlive()) {
            return false;
        } else if (!(target instanceof EntityPlayerMP)) {
            return false;
        } else if (eyes.isPlayerLookingInMyGeneralDirection() || eyes.getBrightness() <= 0) {
            return false;
        }

        return true;
    }

    @Override
    public boolean continueExecuting() {
        if (eyes.isPlayerLookingInMyGeneralDirection() || eyes.getBrightness() <= 0) {
            return false;
        }

        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.courseChangeCooldown = 0;
        // Initialize waypoints to target position
        updateWaypoints();
    }

    @Override
    public void resetTask() {
        this.target = null;
    }

    private void updateWaypoints() {
        if (target == null) return;

        this.waypointX = target.posX;
        this.waypointY = target.posY + target.getEyeHeight();
        this.waypointZ = target.posZ;
    }

    @Override
    public void updateTask() {
        if (target == null) return;

        // Look at target
        this.eyes.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

        // Get speed based on aggression level
        double speed = eyes.getSpeedFromAggro();

        // Update waypoint to current target position
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown = 4 + this.eyes.getRNG().nextInt(7);
            updateWaypoints();

            // Calculate distance to waypoint
            double dx = this.waypointX - this.eyes.posX;
            double dy = this.waypointY - this.eyes.posY;
            double dz = this.waypointZ - this.eyes.posZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            double distance = Math.sqrt(distanceSq);

            // Normalize direction vector and apply speed
            if (distance > 0) {
                double speedFactor = speed / 10.0; // Adjust for smoother movement
                this.eyes.motionX += (dx / distance) * speedFactor;
                this.eyes.motionY += (dy / distance) * speedFactor;
                this.eyes.motionZ += (dz / distance) * speedFactor;

                // Limit velocity to prevent overshooting
                double maxVelocity = speed / 5.0;
                double velocitySq = this.eyes.motionX * this.eyes.motionX +
                    this.eyes.motionY * this.eyes.motionY +
                    this.eyes.motionZ * this.eyes.motionZ;

                if (velocitySq > maxVelocity * maxVelocity) {
                    double velocityFactor = maxVelocity / Math.sqrt(velocitySq);
                    this.eyes.motionX *= velocityFactor;
                    this.eyes.motionY *= velocityFactor;
                    this.eyes.motionZ *= velocityFactor;
                }
            }
        }

        // Attack logic
        double distanceToTargetSq = this.eyes.getDistanceSq(
            target.posX,
            target.boundingBox.minY,
            target.posZ
        );

        this.attackTick = Math.max(this.attackTick - 1, 0);

        // If close enough to attack
        if (distanceToTargetSq <= this.attackRangeSq && this.attackTick <= 0) {
            this.attackTick = 20; // Same as EntityAIAttackOnCollide

            if (this.eyes.getHeldItem() != null) {
                this.eyes.swingItem();
            }

            this.eyes.attackEntityAsMob(target);
        }
    }
}
