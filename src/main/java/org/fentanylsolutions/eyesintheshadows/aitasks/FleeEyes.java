package org.fentanylsolutions.eyesintheshadows.aitasks;

import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

/*
 * public class FleeEyes extends EntityAIAvoidEntity {
 * EntityCreature entityCreature;
 * Class targetClass;
 * Entity closestLivingEntity;
 * float distanceFromEntity;
 * PathEntity entityPathEntity;
 * private PathNavigate entityPathNavigate;
 * public FleeEyes(EntityCreature entityCreature, Class targetClass, float distanceFromEntity, double farSpeed,
 * double nearSpeed) {
 * super(entityCreature, targetClass, distanceFromEntity, farSpeed, nearSpeed);
 * this.entityCreature = entityCreature;
 * this.targetClass = targetClass;
 * this.distanceFromEntity = distanceFromEntity;
 * }
 * @Override
 * public boolean shouldExecute() {
 * /*if (Config.mobsFleeDormantEyes) {
 * return true;
 * }
 * EntityEyes eyes;
 * if (this.entityCreature.getAttackTarget() instanceof EntityEyes) {
 * eyes = (EntityEyes) this.entityCreature.getAttackTarget();
 * } else {
 * return false;
 * }
 * if (eyes.getBrightness() <= 0) {
 * return false;
 * }
 * if (targetClass == EntityPlayer.class) {
 * if (entityCreature instanceof EntityTameable && ((EntityTameable)entityCreature).isTamed()) {
 * return false;
 * }
 * this.closestLivingEntity = entityCreature.worldObj.getClosestPlayerToEntity(entityCreature, this.distanceFromEntity);
 * if (this.closestLivingEntity == null) {
 * return false;
 * }
 * }
 * else {
 * List list = entityCreature.worldObj.selectEntitiesWithinAABB(targetClass,
 * entityCreature.boundingBox.expand(this.distanceFromEntity, 3.0D, (double)this.distanceFromEntity),
 * this.field_98218_a);
 * if (list.isEmpty()) {
 * return false;
 * }
 * this.closestLivingEntity = (Entity)list.get(0);
 * }
 * Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entityCreature, 16, 7,
 * Vec3.createVectorHelper(this.closestLivingEntity.posX, this.closestLivingEntity.posY,
 * this.closestLivingEntity.posZ));
 * if (vec3 == null) {
 * return false;
 * }
 * else if (this.closestLivingEntity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) <
 * this.closestLivingEntity.getDistanceSqToEntity(entityCreature)) {
 * return false;
 * } else {
 * this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
 * return this.entityPathEntity == null ? false : this.entityPathEntity.isDestinationSame(vec3);
 * }
 * //return super.shouldExecute();
 * }
 * @Override
 * public boolean continueExecuting() {
 * if (Config.mobsFleeDormantEyes) {
 * return true;
 * }
 * EntityEyes eyes;
 * if (this.entityCreature.getAttackTarget() instanceof EntityEyes) {
 * eyes = (EntityEyes) this.entityCreature.getAttackTarget();
 * } else {
 * return false;
 * }
 * if (eyes.getBrightness() <= 0) {
 * return false;
 * }
 * return super.continueExecuting();
 * }
 * }
 */

public class FleeEyes extends EntityAIBase {

    public final IEntitySelector field_98218_a = new IEntitySelector() {

        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isEntityApplicable(Entity p_82704_1_) {
            return p_82704_1_.isEntityAlive() && FleeEyes.this.theEntity.getEntitySenses()
                .canSee(p_82704_1_);
        }
    };

    /** The entity we are attached to */
    private EntityCreature theEntity;
    private double farSpeed;
    private double nearSpeed;
    private Entity closestLivingEntity;
    private float distanceFromEntity;
    /** The PathEntity of our entity */
    private PathEntity entityPathEntity;
    /** The PathNavigate of our entity */
    private PathNavigate entityPathNavigate;
    /** The class of the entity we should avoid */
    private Class targetEntityClass;

    public FleeEyes(EntityCreature theEntity, Class<? extends net.minecraft.entity.Entity> targetEntityClass,
        float distanceFromEntity, double farSpeed, double nearSpeed) {
        this.theEntity = theEntity;
        this.targetEntityClass = targetEntityClass;
        this.distanceFromEntity = distanceFromEntity;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.entityPathNavigate = theEntity.getNavigator();
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        List list = this.theEntity.worldObj.selectEntitiesWithinAABB(
            this.targetEntityClass,
            this.theEntity.boundingBox.expand(this.distanceFromEntity, 3.0D, (double) this.distanceFromEntity),
            this.field_98218_a);

        this.closestLivingEntity = null;

        for (Object e : list) {
            if (e instanceof EntityEyes eyes) {
                if (eyes.getBrightness() > 0) {
                    this.closestLivingEntity = (Entity) e;
                }
            }
        }

        if (this.closestLivingEntity == null) {
            return false;
        }

        Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
            this.theEntity,
            16,
            7,
            Vec3.createVectorHelper(
                this.closestLivingEntity.posX,
                this.closestLivingEntity.posY,
                this.closestLivingEntity.posZ));

        if (vec3 == null) {
            return false;
        } else if (this.closestLivingEntity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord)
            < this.closestLivingEntity.getDistanceSqToEntity(this.theEntity)) {
                return false;
            } else {
                this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                return this.entityPathEntity == null ? false : this.entityPathEntity.isDestinationSame(vec3);
            }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !this.entityPathNavigate.noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.entityPathNavigate.setPath(this.entityPathEntity, this.farSpeed);
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.closestLivingEntity = null;
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        if (this.theEntity.getDistanceSqToEntity(this.closestLivingEntity) < 49.0D) {
            this.theEntity.getNavigator()
                .setSpeed(this.nearSpeed);
        } else {
            this.theEntity.getNavigator()
                .setSpeed(this.farSpeed);
        }
    }
}
