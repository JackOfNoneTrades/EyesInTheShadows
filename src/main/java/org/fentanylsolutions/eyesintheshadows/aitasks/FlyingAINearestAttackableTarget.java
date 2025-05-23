package org.fentanylsolutions.eyesintheshadows.aitasks;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;

import org.apache.commons.lang3.StringUtils;
import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

/**
 * Modified version of EntityAINearestAttackableTarget that works with EntityEyes (which extends EntityFlying)
 */
public class FlyingAINearestAttackableTarget extends EntityAIBase {

    private final EntityFlying taskOwner;
    private final Class targetClass;
    private final int targetChance;
    private final TargetSorter theNearestAttackableTargetSorter;
    private final IEntitySelector targetEntitySelector;
    private EntityLivingBase targetEntity;

    // Targeting settings from EntityAITarget
    protected boolean shouldCheckSight;
    private boolean nearbyOnly;
    private int targetSearchStatus;
    private int targetSearchDelay;
    private int timeHaventSeenTarget;

    public FlyingAINearestAttackableTarget(EntityFlying entity, Class<? extends Entity> targetClass, int targetChance,
        boolean shouldCheckSight) {
        this(entity, targetClass, targetChance, shouldCheckSight, false);
    }

    public FlyingAINearestAttackableTarget(EntityFlying entity, Class<? extends Entity> targetClass, int targetChance,
        boolean shouldCheckSight, boolean nearbyOnly) {
        this(entity, targetClass, targetChance, shouldCheckSight, nearbyOnly, null);
    }

    public FlyingAINearestAttackableTarget(EntityFlying entity, Class<? extends Entity> targetClass, int targetChance,
        boolean shouldCheckSight, boolean nearbyOnly, final IEntitySelector selector) {
        super();
        this.taskOwner = entity;
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.shouldCheckSight = shouldCheckSight;
        this.nearbyOnly = nearbyOnly;
        this.theNearestAttackableTargetSorter = new TargetSorter(entity);
        this.setMutexBits(1);

        // Create an entity selector that combines the provided selector with our own suitable target check
        this.targetEntitySelector = new IEntitySelector() {

            @Override
            public boolean isEntityApplicable(Entity entity) {
                if (!(entity instanceof EntityLivingBase)) {
                    return false;
                }

                if (selector != null && !selector.isEntityApplicable(entity)) {
                    return false;
                }

                return isSuitableTarget((EntityLivingBase) entity, false);
            }
        };
    }

    public EntityLivingBase getTargetEntity() {
        return targetEntity;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG()
            .nextInt(this.targetChance) != 0) {
            return false;
        }

        double targetDistance = this.getTargetDistance();
        List list = this.taskOwner.worldObj.selectEntitiesWithinAABB(
            this.targetClass,
            this.taskOwner.boundingBox.expand(targetDistance, targetDistance, targetDistance),
            this.targetEntitySelector);

        if (list.isEmpty()) {
            return false;
        } else {
            if (list.size() > 1) {
                Collections.sort(list, this.theNearestAttackableTargetSorter);
            }
            this.targetEntity = (EntityLivingBase) list.get(0);
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting() {
        EntityLivingBase target = this.taskOwner.getAttackTarget();

        if (target == null) {
            return false;
        } else if (!target.isEntityAlive()) {
            return false;
        } else {
            double maxDistance = this.getTargetDistance();
            if (this.taskOwner.getDistanceSqToEntity(target) > maxDistance * maxDistance) {
                return false;
            } else {
                if (this.shouldCheckSight) {
                    if (this.taskOwner.getEntitySenses()
                        .canSee(target)) {
                        this.timeHaventSeenTarget = 0;
                    } else if (++this.timeHaventSeenTarget > 60) {
                        return false;
                    }
                }

                return !(target instanceof EntityPlayerMP)
                    || !((EntityPlayerMP) target).theItemInWorldManager.isCreative();
            }
        }
    }

    protected double getTargetDistance() {
        return Config.watchDistance;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        EyesInTheShadows.LOG.info("Set eye target to " + this.targetEntity);
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.timeHaventSeenTarget = 0;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        this.taskOwner.setAttackTarget(null);
        EyesInTheShadows.LOG.info("Reset eye target");
    }

    /**
     * Checks if the target is suitable for the entity
     */
    protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvulnerables) {
        if (target == null) {
            return false;
        } else if (target == this.taskOwner) {
            return false;
        } else if (!target.isEntityAlive()) {
            return false;
        } else if (target.isPotionActive(Potion.invisibility)) {
            return false;
        }

        // Can't target what the entity isn't allowed to attack
        // Note: Need to implement canAttackClass in EntityEyes or rely on other means

        // Check if the target is an owner
        if (this.taskOwner instanceof IEntityOwnable
            && StringUtils.isNotEmpty(((IEntityOwnable) this.taskOwner).func_152113_b())) {
            if (target instanceof IEntityOwnable && ((IEntityOwnable) this.taskOwner).func_152113_b()
                .equals(((IEntityOwnable) target).func_152113_b())) {
                return false;
            }

            if (target == ((IEntityOwnable) this.taskOwner).getOwner()) {
                return false;
            }
        } else if (target instanceof EntityPlayer && !includeInvulnerables
            && ((EntityPlayer) target).capabilities.disableDamage) {
                return false;
            }

        // Check if target is within the eyes home distance
        // Note: For EntityEyes we can skip this check since they're ghost-like entities

        if (this.shouldCheckSight && !this.taskOwner.getEntitySenses()
            .canSee(target)) {
            return false;
        }

        // For flying entities, we'll skip the nearbyOnly check that uses path-based accessibility
        // This simplifies targeting for flying entities that don't use standard pathing

        return true;
    }

    /**
     * Sorter for finding the nearest entity
     */
    public static class TargetSorter implements Comparator<Entity> {

        private final Entity theEntity;

        public TargetSorter(Entity entity) {
            this.theEntity = entity;
        }

        public int compare(Entity entity1, Entity entity2) {
            double d0 = this.theEntity.getDistanceSqToEntity(entity1);
            double d1 = this.theEntity.getDistanceSqToEntity(entity2);
            return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
        }
    }
}
