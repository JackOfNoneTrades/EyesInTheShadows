package org.fentanylsolutions.eyesintheshadows.aitasks;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

/**
 * An AI task for flying entities to avoid certain other entities
 */
public class FlyingAIAvoidEntity extends EntityAIBase {

    private EntityEyes entity;
    private double farSpeed;
    private double nearSpeed;
    private Entity closestLivingEntity;
    private float distanceFromEntity;
    private double escapeX;
    private double escapeY;
    private double escapeZ;
    private Class targetEntityClass;

    private int fleeingTicks = 0;
    private static final int MAX_FLEEING_TICKS = 60;

    public FlyingAIAvoidEntity(EntityEyes entity, Class targetClass, float distance, double farSpeed, double nearSpeed) {
        this.entity = entity;
        this.targetEntityClass = targetClass;
        this.distanceFromEntity = distance;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        // Don't execute if already fleeing (there's a cooldown)
        if (fleeingTicks > 0) {
            fleeingTicks--;
            return false;
        }

        List list = this.entity.worldObj.getEntitiesWithinAABB(
            this.targetEntityClass,
            this.entity.boundingBox.expand(
                this.distanceFromEntity,
                this.distanceFromEntity,
                this.distanceFromEntity
            )
        );

        if (list.isEmpty()) {
            return false;
        }

        this.closestLivingEntity = findClosestEntity(list);

        if (this.closestLivingEntity == null) {
            return false;
        }

        // Find escape position in a random direction, but generally away from the entity
        Vec3 escapeVector = findEscapePosition();

        if (escapeVector == null) {
            return false;
        }

        this.escapeX = escapeVector.xCoord;
        this.escapeY = escapeVector.yCoord;
        this.escapeZ = escapeVector.zCoord;

        return true;
    }


    @Override
    public boolean continueExecuting() {
        // Continue until we've lost the entity or have been fleeing for a while
        return this.fleeingTicks < MAX_FLEEING_TICKS &&
            this.entity.getDistanceSqToEntity(this.closestLivingEntity) <
                (double)(this.distanceFromEntity * this.distanceFromEntity);
    }

    @Override
    public void startExecuting() {
        this.fleeingTicks = 0;
    }

    @Override
    public void resetTask() {
        this.closestLivingEntity = null;
        this.fleeingTicks = 40 + this.entity.getRNG().nextInt(20);
    }

    @Override
    public void updateTask() {
        // Calculate speed based on distance to the entity
        double distanceSq = this.entity.getDistanceSqToEntity(this.closestLivingEntity);
        double maxDistanceSq = this.distanceFromEntity * this.distanceFromEntity;

        // Speed proportional to how close the threat is
        double speedFactor = 1.0 - distanceSq / maxDistanceSq;
        double speed = this.farSpeed + (this.nearSpeed - this.farSpeed) * speedFactor;

        // Calculate direction to escape point
        double dx = this.escapeX - this.entity.posX;
        double dy = this.escapeY - this.entity.posY;
        double dz = this.escapeZ - this.entity.posZ;
        double distance = MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);

        if (distance > 0) {
            double moveSpeed = speed / 10.0;

            this.entity.motionX += (dx / distance) * moveSpeed;
            this.entity.motionY += (dy / distance) * moveSpeed;
            this.entity.motionZ += (dz / distance) * moveSpeed;

            // Cap velocity to prevent overshooting
            double maxVel = speed / 5.0;
            double velSq = this.entity.motionX * this.entity.motionX +
                this.entity.motionY * this.entity.motionY +
                this.entity.motionZ * this.entity.motionZ;

            if (velSq > maxVel * maxVel) {
                double velFactor = maxVel / Math.sqrt(velSq);
                this.entity.motionX *= velFactor;
                this.entity.motionY *= velFactor;
                this.entity.motionZ *= velFactor;
            }
        }

        this.fleeingTicks++;
    }

    /**
     * Find the closest entity from a list
     */
    private Entity findClosestEntity(List entities) {
        Entity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Object obj : entities) {
            Entity entity = (Entity)obj;
            double distSq = this.entity.getDistanceSqToEntity(entity);

            if (distSq < closestDistSq) {
                closest = entity;
                closestDistSq = distSq;
            }
        }

        return closest;
    }

    /**
     * Find a position to escape to
     */
    private Vec3 findEscapePosition() {
        // Calculate vector from threat to entity
        double dx = this.entity.posX - this.closestLivingEntity.posX;
        double dy = this.entity.posY - this.closestLivingEntity.posY;
        double dz = this.entity.posZ - this.closestLivingEntity.posZ;

        dx += this.entity.getRNG().nextGaussian() * 2.0;
        dy += this.entity.getRNG().nextGaussian() * 2.0 + 2.0;
        dz += this.entity.getRNG().nextGaussian() * 2.0;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        // Scale by desired escape distance (10-15 blocks)
        double escapeDistance = 10.0 + this.entity.getRNG().nextDouble() * 5.0;
        dx *= escapeDistance;
        dy *= escapeDistance;
        dz *= escapeDistance;

        // Return the escape point
        return Vec3.createVectorHelper(
            this.entity.posX + dx,
            this.entity.posY + dy,
            this.entity.posZ + dz
        );
    }
}
