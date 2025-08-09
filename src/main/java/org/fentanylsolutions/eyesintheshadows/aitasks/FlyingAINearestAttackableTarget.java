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

    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        // EyesInTheShadows.LOG.debug("Set eye target to " + this.targetEntity);
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.timeHaventSeenTarget = 0;
    }

    @Override
    public void resetTask() {
        this.taskOwner.setAttackTarget(null);
        // EyesInTheShadows.LOG.debug("Reset eye target");
    }

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

        if (this.shouldCheckSight && !this.taskOwner.getEntitySenses()
            .canSee(target)) {
            return false;
        }

        return true;
    }

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
