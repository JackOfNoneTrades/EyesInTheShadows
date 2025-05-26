package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Direction;
import net.minecraft.util.Vec3;

import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

public class FlyingCreepTowardPlayer extends EntityAIBase {

    private final EntityEyes eyes;
    private EntityLivingBase target;
    private int courseChangeCooldown;
    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private int attackTick;
    private double attackRangeSq;
    private int swerving = 0;
    private int lastDir = -1;

    public FlyingCreepTowardPlayer(EntityEyes creature) {
        this.eyes = creature;
        this.setMutexBits(3);
        this.attackRangeSq = 4;
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
            swerving = 0;
            lastDir = -1;
            return false;
        }

        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.courseChangeCooldown = 0;
        updateWaypoints();
    }

    @Override
    public void resetTask() {
        this.target = null;
        swerving = 0;
        lastDir = -1;
    }

    private int[] dirToOffset(int dir) {
        return switch (dir) {
            case 0 -> new int[] { 0, 1 };
            case 1 -> new int[] { -1, 0 };
            case 2 -> new int[] { 0, -1 };
            default -> new int[] { 1, 0 };
        };
    }

    Vec3 applyDirOffset(Vec3 v, int dir) {
        int[] offset = dirToOffset(dir);
        return Vec3.createVectorHelper(v.xCoord + offset[0], v.yCoord, v.zCoord + offset[1]);
    }

    private void updateWaypoints() {
        if (target == null) return;

        this.waypointX = target.posX;
        this.waypointY = target.boundingBox.minY + target.getEyeHeight();
        this.waypointZ = target.posZ;

        Vec3 eyePos = Vec3.createVectorHelper(eyes.posX, eyes.boundingBox.minY - 0.5, eyes.posZ);
        Vec3 targetPos = Vec3
            .createVectorHelper(target.posX, target.boundingBox.minY + (double) target.getEyeHeight(), target.posZ);

        boolean canSee = eyes.worldObj.func_147447_a(eyePos, targetPos, false, true, false) == null;
        if (!canSee) {
            Vec3 eyeLookVec = eyePos.subtract(targetPos);
            int targetDirection = lastDir == -1 ? Direction.getMovementDirection(eyeLookVec.xCoord, eyeLookVec.yCoord)
                : lastDir;
            lastDir = targetDirection;

            Vec3 offsetPos = applyDirOffset(eyePos, targetDirection);

            // check up, left, right to see if we can bypass the obstacle
            offsetPos.yCoord += 15;
            canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;

            if (canSee) {
                swerving = 0;
                lastDir = -1;
                this.waypointY += 15;
                return;
            }

            offsetPos.yCoord -= 15;

            // "SOUTH", "WEST", "NORTH", "EAST"
            if (targetDirection == 0 || targetDirection == 2) {
                offsetPos.xCoord += 15;

                if (swerving == 0 || swerving == 1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = 1;
                        this.waypointX += 15;
                        return;
                    }
                }

                offsetPos.xCoord -= 30;

                if (swerving == 0 || swerving == -1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = -1;
                        this.waypointX -= 15;
                        return;
                    }
                }
            } else {
                offsetPos.zCoord += 15;

                if (swerving == 0 || swerving == 1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = 1;
                        this.waypointZ += 15;
                        return;
                    }
                }

                offsetPos.zCoord -= 30;

                if (swerving == 0 || swerving == -1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = -1;
                        this.waypointZ -= 15;
                        return;
                    }
                }
            }
        } else {
            swerving = 0;
            lastDir = -1;
        }
    }

    @Override
    public void updateTask() {
        if (target == null) return;

        // Look at target
        this.eyes.getLookHelper()
            .setLookPositionWithEntity(target, 30.0F, 30.0F);

        // Get speed based on aggression level
        double speed = eyes.getSpeedFromAggro();

        // Update waypoint to current target position
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown = 4 + this.eyes.getRNG()
                .nextInt(7);
            updateWaypoints();

            // Calculate distance to waypoint
            double dx = this.waypointX - this.eyes.posX;
            double dy = this.waypointY - this.eyes.posY;
            double dz = this.waypointZ - this.eyes.posZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            double distance = Math.sqrt(distanceSq);

            // Normalize direction vector and apply speed
            if (distance > 0.5) {
                double speedFactor = speed;
                this.eyes.motionX += (dx / distance) * speedFactor;
                this.eyes.motionY += (dy / distance) * speedFactor;
                this.eyes.motionZ += (dz / distance) * speedFactor;
                this.courseChangeCooldown += 20;
            }
        }

        // Attack
        double distanceToTargetSq = this.eyes
            .getDistanceSq(target.posX, target.boundingBox.minY + target.getEyeHeight(), target.posZ);

        this.attackTick = Math.max(this.attackTick - 1, 0);

        if (distanceToTargetSq <= this.attackRangeSq && this.attackTick <= 0) {
            this.attackTick = 20;
            this.eyes.attackEntityAsMob(target);
        }
    }
}
