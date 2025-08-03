package org.fentanylsolutions.eyesintheshadows.entity.entities;

import java.util.List;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.aitasks.FlyingAIAttackOnCollide;
import org.fentanylsolutions.eyesintheshadows.aitasks.FlyingAIAvoidEntity;
import org.fentanylsolutions.eyesintheshadows.aitasks.FlyingAINearestAttackableTarget;
import org.fentanylsolutions.eyesintheshadows.aitasks.FlyingCreepTowardPlayer;
import org.fentanylsolutions.eyesintheshadows.aitasks.FlyingEyesWander;
import org.fentanylsolutions.eyesintheshadows.aitasks.FlyingTargetTamedWolves;
import org.fentanylsolutions.eyesintheshadows.entity.EyeSenses;
import org.fentanylsolutions.eyesintheshadows.entity.IModEntity;
import org.fentanylsolutions.eyesintheshadows.mixins.early.minecraft.AccessorEntityLiving;
import org.fentanylsolutions.eyesintheshadows.packet.PacketHandler;
import org.fentanylsolutions.eyesintheshadows.packet.PacketUtil;
import org.fentanylsolutions.eyesintheshadows.packet.packets.InitiateJumpscarePacket;
import org.fentanylsolutions.eyesintheshadows.util.TraceUtil;
import org.fentanylsolutions.eyesintheshadows.util.Util;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class EntityEyes extends EntityFlying implements IModEntity {

    private NBTTagCompound syncDataCompound = new NBTTagCompound();

    /** Random offset used in floating behaviour */
    private float heightOffset = 0.5F;
    /** ticks until heightOffset is randomized */
    private int heightOffsetUpdateTime;

    public final float waveAmplitude = Config.waveMotionMinAmplitude
        + EyesInTheShadows.varInstanceCommon.rand.nextFloat()
            * (Config.waveMotionMaxAmplitude - Config.waveMotionMinAmplitude);
    public final float waveSpeed = Config.waveMotionMinSpeed
        + EyesInTheShadows.varInstanceCommon.rand.nextFloat() * (Config.waveMotionMaxSpeed - Config.waveMotionMinSpeed);

    public EntityEyes(World world) {
        super(world);
        // setSize(1.0F, 0.25F);
        this.setSize(0.6F, 0.6F);
        this.stepHeight = 1.0F;
        /* Setting aggro level depending on difficulty, if enabled in config */
        if (Config.eyeAggressionDependsOnLocalDifficulty) {
            setAggroLevel(
                EyesInTheShadows.varInstanceCommon.rand.nextFloat() * worldObj.difficultySetting.getDifficultyId()
                    / 10);
        }
        initSyncDataCompound();
        setupAI();
        ((AccessorEntityLiving) this).setSenses(new EyeSenses(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.experienceValue = 5;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public boolean isPlayerLookingInMyGeneralDirection() {
        if (this.getBrightness() <= 0) {
            return false;
        }

        float maxWatchDistance = Config.watchDistance;
        Vec3 selfPos = getPosEyes(this);

        List<EntityPlayer> entities = worldObj.getEntitiesWithinAABB(
            EntityPlayer.class,
            AxisAlignedBB.getBoundingBox(
                selfPos.xCoord - maxWatchDistance,
                selfPos.yCoord - maxWatchDistance,
                selfPos.zCoord - maxWatchDistance,
                selfPos.xCoord + maxWatchDistance,
                selfPos.yCoord + maxWatchDistance,
                selfPos.zCoord + maxWatchDistance));

        for (EntityPlayer player : entities) {
            if (player.capabilities.isCreativeMode || player.isPotionActive(Potion.blindness)
                || player.isPotionActive(Potion.invisibility)) {
                continue;
            }
            Vec3 playerPos = Vec3
                .createVectorHelper(player.posX, player.boundingBox.minY + player.getEyeHeight(), player.posZ);
            Vec3 lookVec = player.getLookVec();

            // TODO: normalization might not be necessary
            Vec3 eyeLookVec = selfPos.subtract(playerPos)
                .normalize();

            if (lookVec.dotProduct(eyeLookVec) < 0) {
                return true;
            }
        }

        return false;
    }

    public static Vec3 getPosEyes(EntityEyes eyes) {
        return Vec3.createVectorHelper(eyes.posX, eyes.posY + eyes.getEyeHeight(), eyes.posZ);
    }

    @Override
    public void setupAI() {

        this.tasks.addTask(3, new FlyingAIAvoidEntity(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));

        if (Config.eyeBaseAttackDamage > 0 && Config.eyesAttackTamedWolves) {
            this.targetTasks.addTask(3, new FlyingTargetTamedWolves(this, EntityWolf.class, 0, false));
            this.tasks.addTask(4, new FlyingAIAttackOnCollide(this, EntityWolf.class, 1.0D, true));
        }

        if (!Config.passiveEyes) {
            this.targetTasks.addTask(1, new FlyingAINearestAttackableTarget(this, EntityPlayerMP.class, 0, false));
            this.tasks.addTask(1, new FlyingCreepTowardPlayer(this));
        }
        if (Config.eyesWander) {
            this.tasks.addTask(15, new FlyingEyesWander(this, 1.0D));
        }
        this.tasks.addTask(6, new EntityAILookIdle(this));

        if (Config.eyeBaseAttackDamage > 0) {
            for (Class c : EyesInTheShadows.varInstanceCommon.entitiesAttackedByEyesList) {
                this.targetTasks.addTask(3, new FlyingAINearestAttackableTarget(this, c, 0, false));
                this.tasks.addTask(4, new FlyingAIAttackOnCollide(this, c, 1.0D, true));
            }
        }
        for (Class c : EyesInTheShadows.varInstanceCommon.entitiesThatEyesFleeList) {
            this.tasks.addTask(3, new FlyingAIAvoidEntity(this, c, 6.0F, 1.0D, 1.2D));
        }
    }

    @Override
    public void clearAITasks() {
        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();
    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap()
            .registerAttribute(SharedMonsterAttributes.attackDamage);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(Config.health);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(Config.eyeBaseAttackDamage);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeEntityToNBT(par1NBTTagCompound);
        // store additional custom variables for save, example: par1NBTTagCompound.setBoolean("Angry", isAngry());
    }

    /* (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        // retrieve additional custom variables from save, example: setAngry(par1NBTTagCompound.getBoolean("Angry"));
    }

    protected void fall(float p_70069_1_) {}

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        // this.rotationYawHead = 0;

        if (!this.worldObj.isRemote) {
            if (this.isWet() && Config.damageFromWet > 0) {
                this.attackEntityFrom(DamageSource.drown, Config.damageFromWet);
            }

            if (Config.fly) {
                --this.heightOffsetUpdateTime;

                if (this.heightOffsetUpdateTime <= 0) {
                    this.heightOffsetUpdateTime = 100;
                    this.heightOffset = 0.5F + (float) this.rand.nextGaussian() * 3.0F;
                }

                if (this.getAttackTarget() != null && this.getAttackTarget().posY + (double) this.getAttackTarget()
                    .getEyeHeight() > this.posY + (double) this.getEyeHeight() /* + (double) this.heightOffset */
                    && this.moveForward > 0) {
                    this.motionY += 0.1;// (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
                    this.moveFlying(0, (float) moveForward, 1);
                }
            }
        }

        if (Config.fly) {
            if (!this.onGround && this.motionY < 0.0D) {
                this.motionY *= 0.6D;
            }
        }

        float alpha = Util.getEyeRenderingAlpha(this, Config.eyesCanAttackWhileLit);

        setEyeBrightness(alpha);
        // EyesInTheShadows.debug("Looking in my dir: " + isPlayerLookingInMyGeneralDirection(this));

        // if(worldObj != null && worldObj.isRemote) {
        if (!getBlinkingState()) {
            if (EyesInTheShadows.varInstanceCommon.rand.nextFloat() < Config.blinkChance / 10) {
                setBlinkingState(true);
                setBlinkProgress(0);
            }
        } else {
            setBlinkProgress(getBlinkProgress() + 1);
            if (getBlinkProgress() >= Config.blinkDuration) {
                setBlinkingState(false);
            }
        }
        // }

        /* Making eyes disappear if a player looks at them */
        if (worldObj != null && alpha > 0) {
            Vec3 eyePosEyes = getPosEyes(this);
            if (getBrightness() > 0) {
                float maxWatchDistance = Config.watchDistance;

                List<EntityPlayer> entities = worldObj.getEntitiesWithinAABB(
                    EntityPlayer.class,
                    AxisAlignedBB.getBoundingBox(
                        eyePosEyes.xCoord - maxWatchDistance,
                        eyePosEyes.yCoord - maxWatchDistance,
                        eyePosEyes.zCoord - maxWatchDistance,
                        eyePosEyes.xCoord + maxWatchDistance,
                        eyePosEyes.yCoord + maxWatchDistance,
                        eyePosEyes.zCoord + maxWatchDistance));

                boolean shouldDisappear;
                for (EntityPlayer player : entities) {
                    if ((player.capabilities.isCreativeMode && !player.isSneaking())) {
                        continue;
                    }
                    boolean foundPotion = false;
                    for (Potion p : Config.potionIgnoreDisappearNames) {
                        if (player.isPotionActive(p)) {
                            foundPotion = true;
                            break;
                        }
                    }
                    if (foundPotion) {
                        continue;
                    }
                    Vec3 playerPosEyes = Vec3
                        .createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
                    if (playerPosEyes.distanceTo(eyePosEyes) > maxWatchDistance) {
                        continue;
                    }

                    Vec3 planePos = Vec3.createVectorHelper(this.posX, this.posY + getEyeHeight(), this.posZ);
                    Vec3 vec3 = player.getLook(1.0F)
                        .normalize();
                    float width = 0.25f;
                    float height = width * 5.f / 13f;
                    Vec3 planeNormal = vec3.subtract(planePos)
                        .normalize();
                    double denom = planeNormal.dotProduct(vec3);
                    double approxHitDist = -1;
                    if (Math.abs(denom) > 1e-6) { // Make sure not parallel
                        Vec3 diff = planePos.subtract(playerPosEyes);
                        double t = Math.abs(diff.dotProduct(planeNormal) / denom);

                        Vec3 hitPoint = playerPosEyes.addVector(vec3.xCoord * t, vec3.yCoord * t, vec3.zCoord * t);

                        // TODO: for better accuracy find actual x and y delta
                        // Step 2: Check if hitPoint is within bounding box of the plane
                        // Transform hitPoint into local 2D coordinates relative to plane center and axes
                        // For example, use right and up vectors of the plane:

                        // Vec3 right = ...; // e.g., a vector along the width
                        // Vec3 up = ...; // a vector along the height

                        // Vec3 local = hitPoint.subtract(planePos);
                        // double xDist = local.dotProduct(right);
                        // double yDist = local.dotProduct(up);

                        // if (Math.abs(xDist) <= width / 2 && Math.abs(yDist) <= height / 2) {
                        // ✅ Hit is inside the bounds of the plane
                        // }
                        approxHitDist = hitPoint.distanceTo(planePos);
                    }

                    // Old code
                    /*
                     * Vec3 vec31 = Vec3.createVectorHelper(
                     * this.posX - player.posX,
                     * (this.posY + getEyeHeight())
                     * - (player.posY + (double) player.getEyeHeight()),
                     * this.posZ - player.posZ);
                     * double d0 = vec31.lengthVector();
                     * vec31 = vec31.normalize();
                     * double d1 = vec3.dotProduct(vec31);
                     * shouldDisappear = d1 > 1.0D - 0.025D / d0 && player.canEntityBeSeen(this);
                     */
                    shouldDisappear = approxHitDist <= width
                        && TraceUtil.canEntityBeSeenIgnoreWithoutBoundingBox(player, this);

                    if (shouldDisappear) {
                        disappear(true);
                        if (Config.potionLookNames.length > 0) {
                            Potion p = Config.potionLookNames[EyesInTheShadows.varInstanceCommon.rand
                                .nextInt(Config.potionLookNames.length)];
                            player.addPotionEffect(
                                new PotionEffect(
                                    p.getId(),
                                    Config.potionLookDuration * 20,
                                    Config.potionLookAmplifier));

                        }
                    }
                }
            }
        }

        if (Config.enableEyeAggressionEscalation && alpha > 0) {
            if (!isPlayerLookingInMyGeneralDirection() && this.getAttackTarget() != null) {
                setAggroLevel(getAggroLevel() + Config.aggroEscalationPerTick);
                if (Config.eyeAggressionDependsOnLightLevel) {
                    setAggroLevel(getAggroLevel() + Config.aggroEscalationPerTick * alpha);
                }
            } else {
                setAggroLevel(getAggroLevel() - Config.aggroEscalationPerTick * 2);
                /*
                 * if (Config.eyeAggressionDependsOnLightLevel) {
                 * setAggroLevel(getAggroLevel() - Config.aggroEscalationPerTick * alpha);
                 * }
                 */
            }
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        /// return false;
        disappear(false);
        return true;

        /*
         * // DEBUG!!!!!
         * if (par1DamageSource.getEntity() instanceof EntityPlayerMP && EyesInTheShadows.isDebugMode()) {
         * jumpscare((EntityPlayerMP) par1DamageSource.getEntity());
         * }
         * if (isEntityInvulnerable()) {
         * return false;
         * } else {
         * return super.attackEntityFrom(par1DamageSource, par2);
         * }
         */
    }

    @Override
    public boolean attackEntityAsMob(Entity attackedEntity) {
        boolean jumpScared = Config.jumpscare && attackedEntity instanceof EntityPlayerMP;
        if (jumpScared) {
            if (Config.potionNames.length > 0) {
                Potion p = Config.potionNames[EyesInTheShadows.varInstanceCommon.rand
                    .nextInt(Config.potionNames.length)];
                ((EntityLivingBase) attackedEntity)
                    .addPotionEffect(new PotionEffect(p.getId(), Config.potionDuration * 20, Config.potionAmplifier));
            }
            jumpscare((EntityPlayerMP) attackedEntity);
            disappear(false);
        }

        /*
         * if (this.attackTime == 0) {
         * this.attackTime = Config.tickBetweenAttacks;
         * super.attackEntityAsMob(attackedEntity);
         * // jabelar says this is correct :shrug:
         * this.setLastAttacker(attackedEntity);
         * }
         * // stub return
         * return false;
         */

        float f = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage)
            .getAttributeValue();
        int i = 0;

        if (attackedEntity instanceof EntityLivingBase) {
            f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) attackedEntity);
            i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) attackedEntity);
        }

        boolean flag = attackedEntity.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0) {
                attackedEntity.addVelocity(
                    (double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F),
                    0.1D,
                    (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                attackedEntity.setFire(j * 4);
            }

            if (attackedEntity instanceof EntityLivingBase) {
                EnchantmentHelper.func_151384_a((EntityLivingBase) attackedEntity, this);
            }

            EnchantmentHelper.func_151385_b(this, attackedEntity);
        }

        disappear(true);
        return flag;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return Config.maxSpawnedInChunk;
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn() {
        return ticksExisted > Config.despawnAfterAmountOfTicks;
    }

    // *****************************************************
    // ENCAPSULATION SETTER AND GETTER METHODS
    // Don't forget to send sync packets in setters
    // *****************************************************

    @Override
    public void setScaleFactor(float parScaleFactor) {
        syncDataCompound.setFloat("scaleFactor", Math.abs(parScaleFactor));

        // don't forget to sync client and server
        sendEntitySyncPacket();
    }

    @Override
    public float getScaleFactor() {
        return syncDataCompound.getFloat("scaleFactor");
    }

    public void setBlinkingState(boolean value) {
        syncDataCompound.setBoolean("blinkingState", value);
        sendEntitySyncPacket();
    }

    public boolean getBlinkingState() {
        return syncDataCompound.getBoolean("blinkingState");
    }

    public void setBlinkProgress(float value) {
        syncDataCompound.setFloat("blinkProgress", value);
        sendEntitySyncPacket();
    }

    public float getBlinkProgress() {
        return syncDataCompound.getFloat("blinkProgress");
    }

    public void setEyeBrightness(float brightness) {
        syncDataCompound.setFloat("brightness", brightness);
        sendEntitySyncPacket();
    }

    public float getBrightness() {
        return syncDataCompound.getFloat("brightness");
    }

    public void setAggroLevel(float aggro) {
        syncDataCompound.setFloat("aggro", MathHelper.clamp_float(aggro, 0, 1));
        sendEntitySyncPacket();
    }

    public float getAggroLevel() {
        return syncDataCompound.getFloat("aggro");
    }

    @Override
    public void sendEntitySyncPacket() {
        PacketUtil.sendEntitySyncPacketToClient(this);
    }

    @Override
    public NBTTagCompound getSyncDataCompound() {
        return syncDataCompound;
    }

    @Override
    public void setSyncDataCompound(NBTTagCompound parCompound) {
        syncDataCompound = parCompound;
    }

    /*
     * (non-Javadoc)
     * @see com.blogspot.jabelarminecraft.wildanimals.entities.IModEntity#initSyncDataCompound()
     */
    @Override
    public void initSyncDataCompound() {
        syncDataCompound.setFloat("scaleFactor", 1F);
        syncDataCompound.setInteger("targetId", -1);
        // syncDataCompound.setBoolean("hasTarget", false);
        // syncDataCompound.setDouble("targetX", 0);
        // syncDataCompound.setDouble("targetY", 0);
        // syncDataCompound.setDouble("targetZ", 0);
    }

    /*
     * public Vec3 getTargetPosition() {
     * if (syncDataCompound.getBoolean("hasTarget")) {
     * return Vec3.createVectorHelper(
     * syncDataCompound.getDouble("targetX"),
     * syncDataCompound.getDouble("targetY"),
     * syncDataCompound.getDouble("targetZ"));
     * }
     * return null;
     * }
     */

    public int getTargetId() {
        return syncDataCompound.getInteger("targetId");
    }

    public double getSpeedFromAggro() {
        if (Util.getEyeRenderingAlpha(this, Config.eyesCanAttackWhileLit) <= 0) {
            return 0;
        }
        // return Config.speedNoAggro + (Config.speedFullAggro - Config.speedNoAggro) * getAggroLevel();
        return Util.clampedLerp(getAggroLevel(), Config.speedNoAggro, Config.speedFullAggro);
    }

    @Override
    public void setAttackTarget(EntityLivingBase entity) {
        super.setAttackTarget(entity);
        if (entity == null) {
            syncDataCompound.setInteger("targetId", -1);
        } else {
            syncDataCompound.setInteger("targetId", entity.getEntityId());
        }
        /*
         * syncDataCompound.setBoolean("hasTarget", entity != null);
         * if (entity != null) {
         * syncDataCompound.setDouble("targetX", entity.posX);
         * syncDataCompound.setDouble("targetY", entity.boundingBox.minY + entity.getEyeHeight());
         * syncDataCompound.setDouble("targetZ", entity.posZ);
         * }
         */
    }

    @Override
    protected String getDeathSound() {
        return EyesInTheShadows.varInstanceCommon.disappearSound;
    }

    @Override
    protected String getLivingSound() {
        if (syncDataCompound.getBoolean("hasTarget") && !isPlayerLookingInMyGeneralDirection()) {
            return null;
        }
        return EyesInTheShadows.varInstanceCommon.laughSound;
    }

    protected float getSoundVolume() {
        return Config.eyeIdleVolume;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayer && !((EntityPlayer) entityIn).capabilities.isCreativeMode) {
            disappear(true);
            if (Config.potionCollisionNames.length > 0) {
                Potion p = Config.potionCollisionNames[EyesInTheShadows.varInstanceCommon.rand
                    .nextInt(Config.potionCollisionNames.length)];

                ((EntityLivingBase) entityIn).addPotionEffect(
                    new PotionEffect(p.getId(), Config.potionCollisionDuration * 20, Config.potionCollisionAmplifier));
            }
        }
        // super.collideWithEntity(entityIn);
    }

    private void disappear(boolean playDeathSound) {
        this.setHealth(1);
        damageEntity(DamageSource.outOfWorld, 1);
        if (playDeathSound) {
            this.playSound(getDeathSound(), Config.eyeDisappearVolume, this.getSoundPitch());
        }
    }

    public void jumpscare(EntityPlayerMP player) {
        IMessage msg = new InitiateJumpscarePacket.SimpleMessage(player.posX, player.posY, player.posZ);
        PacketHandler.net.sendTo(msg, player);
    }

    /*
     * So basically MC draws non-translucent entities first, (pass 0), and then
     * translucent blocks, like water (render pass 1).
     * by default, mobs render on render pass 1. But this one has transparent parts and water behind would
     * look weird. Setting it to one helps a little bit. If there is water between the entity and the camera,
     * the entity will look like if it was before the water, but it's still better than default.
     * https://forums.minecraftforge.net/topic/26876-solvedtransparent-texture-in-entities-without-ruining-water-render/
     */
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    protected void onDeathUpdate() {
        this.setDead();
        for (int i = 0; i < 5; ++i) {
            double d2 = this.rand.nextGaussian() * 0.01D;
            double d0 = this.rand.nextGaussian() * 0.01D;
            double d1 = this.rand.nextGaussian() * 0.01D;
            this.worldObj.spawnParticle(
                "explode",
                this.posX + (double) (this.rand.nextFloat() * this.width / 2) - (double) this.width / 2,
                this.posY + (double) (this.rand.nextFloat() * this.height / 2),
                this.posZ + (double) (this.rand.nextFloat() * this.width / 2) - (double) this.width / 2,
                d2,
                d0,
                d1);
        }
    }

    /* The whole gimmick is that it should be a ghost-like entity, duh. */
    @Override
    public boolean canBePushed() {
        return false;
    }

    public float getEyeHeight() {
        // return this.height * EyesInTheShadows.varInstanceClient.hmod;//0.03F;
        return this.height; // * 0.85F;// EyesInTheShadows.varInstanceClient.hmod;
    }

    @Override
    public boolean isEntityUndead() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void collideWithNearbyEntities() {
        /* We need to add 0.2 to Y otherwise we can stand on the mob without triggering a collision */
        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(
            this,
            this.boundingBox.expand(0.20000000298023224D, 0.2D, 0.20000000298023224D));

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
                Entity entity = (Entity) list.get(i);

                if (entity.canBePushed()) {
                    this.collideWithEntity(entity);
                }
            }
        }
    }
}
