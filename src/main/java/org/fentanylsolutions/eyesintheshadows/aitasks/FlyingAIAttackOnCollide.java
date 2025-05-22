package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class FlyingAIAttackOnCollide extends EntityAIBase {

    private final EntityFlying attacker;
    private EntityLivingBase target;
    /** An amount of decrementing ticks that allows the entity to attack once the tick reaches 0. */
    private int attackTick;
    /** The speed with which the mob will approach the target */
    private double speedTowardsTarget;
    /** When true, the mob will continue chasing its target, even if it can't find a path to them right now. */
    private boolean longMemory;
    private int courseChangeCooldown;
    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private int failedAttackPenalty;
    private double attackRangeSq;
    private Class<? extends net.minecraft.entity.Entity> classTarget;


    public FlyingAIAttackOnCollide(EntityFlying attacker, Class<? extends net.minecraft.entity.Entity> targetClass, double speed, boolean longMemory) {
        this.attacker = attacker;
        this.classTarget = targetClass;
        this.speedTowardsTarget = speed;
        this.longMemory = longMemory;
        this.setMutexBits(3);
        this.attackRangeSq = Math.pow(attacker.width * 2.0F * attacker.width * 2.0F, 2);
    }

    public FlyingAIAttackOnCollide(EntityFlying attacker, double speed, boolean longMemory) {
        this.attacker = attacker;
        this.speedTowardsTarget = speed;
        this.longMemory = longMemory;
        this.setMutexBits(3);
        this.attackRangeSq = Math.pow(attacker.width * 2.0F * attacker.width * 2.0F, 2);
    }

    private boolean canSeeTarget() {
        if (target == null) {
            return false;
        }
        return this.attacker.getEntitySenses().canSee(this.target);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        target = this.attacker.getAttackTarget();

        if (target == null)
        {
            return false;
        }
        else if (!target.isEntityAlive())
        {
            return false;
        }
        else if (this.classTarget != null && !this.classTarget.isAssignableFrom(target.getClass()))
        {
            return false;
        }
        return canSeeTarget();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return target != null && target.isEntityAlive() &&
            (this.longMemory || this.canSeeTarget());
    }

    private void updateWaypoints() {
        if (target == null) return;

        this.waypointX = target.posX;
        this.waypointY = target.posY + target.getEyeHeight();
        this.waypointZ = target.posZ;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.courseChangeCooldown = 0;
        this.updateWaypoints();
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.target = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
        if (target == null) return;

        // Look at target
        this.attacker.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

        // Update waypoint to current target position
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown = 4 + this.attacker.getRNG().nextInt(7);

            // Check distance to decide whether to recalculate waypoints
            double distanceSq = this.attacker.getDistanceSq(
                target.posX,
                target.boundingBox.minY,
                target.posZ
            );

            if (distanceSq > 1024.0D) {
                this.courseChangeCooldown += 10;
            } else if (distanceSq > 256.0D) {
                this.courseChangeCooldown += 5;
            }

            if (!this.canSeeTarget() && this.attacker.getRNG().nextFloat() < 0.05F) {
                this.failedAttackPenalty += 10;
            } else {
                this.failedAttackPenalty = 0;
            }

            updateWaypoints();

            // Calculate direction to waypoint
            double dx = this.waypointX - this.attacker.posX;
            double dy = this.waypointY - this.attacker.posY;
            double dz = this.waypointZ - this.attacker.posZ;
            double distanceToWaypoint = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Normalize direction vector and apply speed
            if (distanceToWaypoint > 0) {
                double speedFactor = this.speedTowardsTarget / 10.0; // Adjust for smoother movement
                this.attacker.motionX += (dx / distanceToWaypoint) * speedFactor;
                this.attacker.motionY += (dy / distanceToWaypoint) * speedFactor;
                this.attacker.motionZ += (dz / distanceToWaypoint) * speedFactor;

                // Limit velocity to prevent overshooting
                double maxVelocity = this.speedTowardsTarget / 5.0;
                double velocitySq = this.attacker.motionX * this.attacker.motionX +
                    this.attacker.motionY * this.attacker.motionY +
                    this.attacker.motionZ * this.attacker.motionZ;

                if (velocitySq > maxVelocity * maxVelocity) {
                    double velocityFactor = maxVelocity / Math.sqrt(velocitySq);
                    this.attacker.motionX *= velocityFactor;
                    this.attacker.motionY *= velocityFactor;
                    this.attacker.motionZ *= velocityFactor;
                }
            }
        }
        // Attack logic
        double distanceToTargetSq = this.attacker.getDistanceSq(
            target.posX,
            target.boundingBox.minY,
            target.posZ
        );

        double attackRange = (this.attacker.width * 2.0F * this.attacker.width * 2.0F + target.width);

        this.attackTick = Math.max(this.attackTick - 1, 0);

        // If close enough to attack
        if (distanceToTargetSq <= attackRange && this.attackTick <= 0) {
            this.attackTick = 20;

            if (this.attacker.getHeldItem() != null) {
                this.attacker.swingItem();
            }

            this.attacker.attackEntityAsMob(target);
        }
    }
}
