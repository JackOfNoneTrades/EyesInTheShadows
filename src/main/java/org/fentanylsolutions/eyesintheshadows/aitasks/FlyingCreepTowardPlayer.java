package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Direction;
import net.minecraft.util.Vec3;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

public class FlyingCreepTowardPlayer extends EntityAIBase {

    private static final double BEHIND_DISTANCE = 4.0D;
    private static final double BEHIND_CONE_DOT = -0.35D;
    private static final double VERTICAL_ALIGNMENT_THRESHOLD = 1.25D;
    private static final double OBSTACLE_DETOUR_DISTANCE = 6.0D;

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
            stopMovement();
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
        stopMovement();
        this.target = null;
        swerving = 0;
        lastDir = -1;
    }

    private void stopMovement() {
        eyes.motionX = 0;
        eyes.motionY = 0;
        eyes.motionZ = 0;
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

        Vec3 eyePos = EntityEyes.getPosEyes(eyes);
        double targetEyeY = target.boundingBox.minY + target.getEyeHeight();
        double[] targetLook = getTargetHorizontalLook();

        if (isBehindTarget(targetLook)) {
            this.waypointX = target.posX;
            this.waypointZ = target.posZ;
        } else {
            this.waypointX = target.posX - targetLook[0] * BEHIND_DISTANCE;
            this.waypointZ = target.posZ - targetLook[1] * BEHIND_DISTANCE;
        }
        this.waypointY = Config.fly ? targetEyeY - eyes.getEyeHeight() : eyes.posY;

        Vec3 targetPos = Vec3.createVectorHelper(this.waypointX, targetEyeY, this.waypointZ);

        boolean canSee = eyes.worldObj.func_147447_a(eyePos, targetPos, false, true, false) == null;
        if (!canSee) {
            Vec3 eyeLookVec = eyePos.subtract(targetPos);
            int targetDirection = lastDir == -1 ? Direction.getMovementDirection(eyeLookVec.xCoord, eyeLookVec.zCoord)
                : lastDir;
            lastDir = targetDirection;

            Vec3 offsetPos = applyDirOffset(eyePos, targetDirection);

            // "SOUTH", "WEST", "NORTH", "EAST"
            if (targetDirection == 0 || targetDirection == 2) {
                offsetPos.xCoord += OBSTACLE_DETOUR_DISTANCE;

                if (swerving == 0 || swerving == 1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = 1;
                        this.waypointX += OBSTACLE_DETOUR_DISTANCE;
                        return;
                    }
                }

                offsetPos.xCoord -= OBSTACLE_DETOUR_DISTANCE * 2;

                if (swerving == 0 || swerving == -1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = -1;
                        this.waypointX -= OBSTACLE_DETOUR_DISTANCE;
                        return;
                    }
                }
            } else {
                offsetPos.zCoord += OBSTACLE_DETOUR_DISTANCE;

                if (swerving == 0 || swerving == 1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = 1;
                        this.waypointZ += OBSTACLE_DETOUR_DISTANCE;
                        return;
                    }
                }

                offsetPos.zCoord -= OBSTACLE_DETOUR_DISTANCE * 2;

                if (swerving == 0 || swerving == -1) {
                    canSee = eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null;
                    if (canSee) {
                        swerving = -1;
                        this.waypointZ -= OBSTACLE_DETOUR_DISTANCE;
                        return;
                    }
                }
            }

            if (Config.fly) {
                offsetPos = applyDirOffset(eyePos, targetDirection);
                double verticalDetour = targetEyeY < eyePos.yCoord ? -OBSTACLE_DETOUR_DISTANCE
                    : OBSTACLE_DETOUR_DISTANCE;
                offsetPos.yCoord += verticalDetour;
                if (eyes.worldObj.func_147447_a(offsetPos, targetPos, false, true, false) == null) {
                    swerving = 0;
                    lastDir = -1;
                    this.waypointY += verticalDetour;
                }
            }
        } else {
            swerving = 0;
            lastDir = -1;
        }
    }

    private double[] getTargetHorizontalLook() {
        Vec3 look = target.getLookVec();
        double length = Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord);
        if (length < 1.0E-4D) {
            double yaw = Math.toRadians(target.rotationYaw);
            return new double[] { -Math.sin(yaw), Math.cos(yaw) };
        }
        return new double[] { look.xCoord / length, look.zCoord / length };
    }

    private boolean isBehindTarget(double[] targetLook) {
        double targetToEyesX = eyes.posX - target.posX;
        double targetToEyesZ = eyes.posZ - target.posZ;
        double horizontalDistance = Math.sqrt(targetToEyesX * targetToEyesX + targetToEyesZ * targetToEyesZ);
        return horizontalDistance > 1.0E-4D
            && (targetToEyesX * targetLook[0] + targetToEyesZ * targetLook[1]) / horizontalDistance < BEHIND_CONE_DOT;
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

            double eyeY = this.eyes.posY + this.eyes.getEyeHeight();
            double targetEyeY = target.boundingBox.minY + target.getEyeHeight();
            if (Config.fly && Math.abs(targetEyeY - eyeY) > VERTICAL_ALIGNMENT_THRESHOLD) {
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                double maximumHorizontalComponent = Math.abs(dy) * 0.5D;
                if (horizontalDistance > maximumHorizontalComponent) {
                    double horizontalScale = maximumHorizontalComponent / horizontalDistance;
                    dx *= horizontalScale;
                    dz *= horizontalScale;
                }
            }

            double distanceSq = dx * dx + dy * dy + dz * dz;
            double distance = Math.sqrt(distanceSq);

            // Normalize direction vector and apply speed
            if (distance > 0.5) {
                double speedFactor = speed;
                this.eyes.motionX += (dx / distance) * speedFactor;
                this.eyes.motionY += (dy / distance) * speedFactor;
                this.eyes.motionZ += (dz / distance) * speedFactor;
                this.courseChangeCooldown += 5;
            }
        }

        // Attack
        double distanceToTargetSq = this.eyes
            .getDistanceSq(target.posX, target.boundingBox.minY + target.getEyeHeight(), target.posZ);

        this.attackTick = Math.max(this.attackTick - 1, 0);

        if (distanceToTargetSq <= this.attackRangeSq && this.attackTick <= 0
            && isBehindTarget(getTargetHorizontalLook())) {
            this.attackTick = Config.tickBetweenAttacks;
            this.eyes.attackEntityAsMob(target);
        }
    }
}
