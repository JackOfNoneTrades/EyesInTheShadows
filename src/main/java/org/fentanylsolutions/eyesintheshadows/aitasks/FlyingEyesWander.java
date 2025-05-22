package org.fentanylsolutions.eyesintheshadows.aitasks;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

public class FlyingEyesWander extends EntityAIBase {
    private EntityEyes eyes;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private double speed;
    private int executionChance;
    private boolean mustUpdate;
    private int courseChangeCooldown;

    public FlyingEyesWander(EntityEyes eyes, double speed) {
        this(eyes, speed, 600);
    }

    public FlyingEyesWander(EntityEyes eyes, double speed, int chance) {
        this.eyes = eyes;
        this.speed = speed;
        this.executionChance = chance;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        // If the entity has a target, don't wander
        if (this.eyes.getAttackTarget() != null) {
            return false;
        }

        // If player is looking at me or I'm in bright light, don't wander
        if (eyes.isPlayerLookingInMyGeneralDirection() || eyes.getBrightness() <= 0) {
            return false;
        }

        // Random chance to execute
        if (!this.mustUpdate) {
            if (this.eyes.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
        }

        // Find random position to move to
        double[] pos = getRandomPosition();

        if (pos != null) {
            this.xPosition = pos[0];
            this.yPosition = pos[1];
            this.zPosition = pos[2];
            this.mustUpdate = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean continueExecuting() {
        // If the entity gets a target or player is looking at it, stop wandering
        if (this.eyes.getAttackTarget() != null ||
            eyes.isPlayerLookingInMyGeneralDirection() ||
            eyes.getBrightness() <= 0) {
            return false;
        }

        // Continue until we reach the destination or get relatively close
        double distSq = this.eyes.getDistanceSq(this.xPosition, this.yPosition, this.zPosition);
        return distSq > 4.0;
    }

    @Override
    public void startExecuting() {
        this.courseChangeCooldown = 0;
    }

    @Override
    public void resetTask() {
        this.mustUpdate = true;
    }

    @Override
    public void updateTask() {
        // Calculate direction to target waypoint
        double dx = this.xPosition - this.eyes.posX;
        double dy = this.yPosition - this.eyes.posY;
        double dz = this.zPosition - this.eyes.posZ;
        double distance = MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);

        // Update movement direction periodically
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown = 10 + this.eyes.getRNG().nextInt(400);
        }

        if (distance > 0) {
            // Apply movement forces toward target position
            double speedFactor = this.speed / 80.0; // Scale down for smoother movement

            this.eyes.motionX += (dx / distance) * speedFactor;
            this.eyes.motionY += (dy / distance) * speedFactor;
            this.eyes.motionZ += (dz / distance) * speedFactor;

            // Limit velocity to prevent overshooting
            double maxVelocity = this.speed / 10.0;
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

        // If we're getting close to the target, slow down to avoid overshooting
        /*if (distance < 4.0) {
            this.eyes.motionX *= 0.8;
            this.eyes.motionY *= 0.8;
            this.eyes.motionZ *= 0.8;
        }*/
    }

    private double[] getRandomPosition() {
        // Try to find a valid position to move to
        for (int i = 0; i < 10; i++) {
            // Random position within 10 blocks horizontally and 5 blocks vertically
            double x = this.eyes.posX + (this.eyes.getRNG().nextFloat() * 2.0F - 1.0F) * 10.0F;
            double z = this.eyes.posZ + (this.eyes.getRNG().nextFloat() * 2.0F - 1.0F) * 10.0F;

            double y = this.eyes.posY + 5.0F;

            // Check if the position is in the air (not inside a block)
            /*if (!this.eyes.worldObj.getCollidingBoundingBoxes(
                this.eyes,
                AxisAlignedBB.getBoundingBox(x-0.5, y-0.5, z-0.5, x+0.5, y+0.5, z+0.5)
            ).isEmpty()) {
                continue;
            }

            // Prefer positions in darker areas
            float brightness = this.eyes.worldObj.getLightBrightness(
                MathHelper.floor_double(x),
                MathHelper.floor_double(y),
                MathHelper.floor_double(z)
            );

            // Lower brightness = better (more chance to accept this position)
            if (this.eyes.getRNG().nextFloat() < (1.0F - brightness) * 1.5F) {
                return new double[]{x, y, z};
            }*/

            boolean foundGround = false;
            while (y > this.eyes.posY - 10.0F && !foundGround) {
                if (!this.eyes.worldObj.isAirBlock(
                    MathHelper.floor_double(x),
                    MathHelper.floor_double(y - 1),
                    MathHelper.floor_double(z))) {
                    foundGround = true;
                } else {
                    y -= 1.0F;
                }
            }

            if (foundGround) {
                y += 1.0F;

                if (this.eyes.worldObj.getCollidingBoundingBoxes(
                    this.eyes,
                    AxisAlignedBB.getBoundingBox(x-0.5, y-0.5, z-0.5, x+0.5, y+0.5, z+0.5)
                ).isEmpty()) {
                    // prefer dark spots
                    float brightness = this.eyes.worldObj.getLightBrightness(
                        MathHelper.floor_double(x),
                        MathHelper.floor_double(y),
                        MathHelper.floor_double(z)
                    );
                    if (this.eyes.getRNG().nextFloat() < (1.0F - brightness) * 1.5F) {
                        return new double[]{x, y, z};
                    }
                }
            }
        }

        return null;
    }
}
