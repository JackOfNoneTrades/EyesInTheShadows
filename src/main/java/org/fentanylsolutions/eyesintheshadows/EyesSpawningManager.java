package org.fentanylsolutions.eyesintheshadows;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;

import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.fentanylsolutions.eyesintheshadows.util.TimeUtil;
import org.fentanylsolutions.eyesintheshadows.util.Util;

import com.google.common.base.Stopwatch;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class EyesSpawningManager {

    private final Stopwatch watch = Stopwatch.createUnstarted();
    // private final ServerChunkCache chunkSource;
    private int cooldown;
    private int ticks;

    public EyesSpawningManager() {}

    public static boolean allowSpawnMonsters(World world) {
        return world.difficultySetting != EnumDifficulty.PEACEFUL || Config.passiveEyes;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        // System.out.println("SNEED");
        if (event.side.isClient()) {
            return;
        }
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        tick(event.world);
    }

    private void tick(World world) {
        if (--cooldown > 0) {
            return;
        }

        cooldown = 150;

        if (!Config.enableNaturalSpawn || !world.getGameRules()
            .getGameRuleBooleanValue("doMobSpawning")) {
            return;
        }

        if (!allowSpawnMonsters(world)) {
            return;
        }

        if (!isDimensionAllowed(world)) {
            return;
        }

        try {
            watch.start();

            ticks++;

            int daysUntilNextHalloween = EyesInTheShadows.varInstanceCommon.daysUntilHalloween;
            int minutesToMidnight = TimeUtil.getMinutesToMidnight();

            cooldown = calculateSpawnCycleInterval(daysUntilNextHalloween, minutesToMidnight);

            int maxTotalEyesPerDimension = calculateMaxTotalEyesPerDimension(daysUntilNextHalloween, minutesToMidnight);
            int maxEyesAroundPlayer = calculateMaxEyesAroundPlayer(daysUntilNextHalloween, minutesToMidnight);

            int count = world.countEntities(EntityEyes.class);
            if (count >= maxTotalEyesPerDimension) {
                return;
            }

            float d = Config.maxEyesSpawnDistance * 1.5f;
            float dSqr = d * d;

            List<EntityPlayer> players = world.playerEntities;
            int wrap = Math.min(players.size(), 20);
            for (EntityPlayer player : players) {
                if (((player.getEntityId() + ticks) % wrap) == 0 && !player.capabilities.isCreativeMode) {
                    AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
                        player.posX - d,
                        player.posY - d,
                        player.posZ - d,
                        player.posX + d,
                        player.posY + d,
                        player.posZ + d);
                    List<EntityEyes> entities = world.getEntitiesWithinAABB(EntityEyes.class, searchBox);

                    int nearbyCount = 0;
                    for (EntityEyes eye : entities) {
                        if (eye.getDistanceSqToEntity(player) <= dSqr) {
                            nearbyCount++;
                        }
                    }

                    if (Config.spawnUnderCover
                        && world.canBlockSeeTheSky((int) player.posX, (int) player.posY, (int) player.posZ)) {
                        continue;
                    }

                    if (Util.getBrightnessAtCoord(world, (int) player.posX, (int) player.posY, (int) player.posZ, false)
                        > Config.maxSpawnLightLevel) {
                        continue;
                    }

                    if (!isBiomeAllowed(world, player)) {
                        continue;
                    }

                    if (nearbyCount < maxEyesAroundPlayer) {
                        spawnOneAround(
                            Vec3.createVectorHelper(player.posX, player.posY, player.posZ),
                            player,
                            Config.maxEyesSpawnDistance);
                    }
                }
            }

        } finally {
            watch.stop();

            long elapsedMs = watch.elapsed(TimeUnit.MILLISECONDS);
            if (elapsedMs > Config.spawnCycleSpawnWarningTime) {
                EyesInTheShadows.LOG.warn("WARNING: Unexpectedly long spawn cycle. It ran for " + elapsedMs + "ms!");
            }
        }

        watch.reset();
    }

    private static boolean isDimensionAllowed(World world) {
        return isNameAllowed(world.getProviderName(), Config.dimensionSpawnNames, Config.dimensionListIsWhitelist);
    }

    private static boolean isBiomeAllowed(World world, EntityPlayer player) {
        String biomeName = world.getBiomeGenForCoords((int) player.posX, (int) player.posZ).biomeName;
        return isNameAllowed(biomeName, Config.biomeSpawnNames, Config.biomeListIsWhitelist);
    }

    private static boolean isNameAllowed(String value, String[] list, boolean whitelist) {
        if (list == null || list.length == 0) {
            return true;
        }

        boolean found = false;
        for (String entry : list) {
            if (value.equals(entry)) {
                found = true;
                break;
            }
        }

        return whitelist ? found : !found;
    }

    private int calculateSpawnCycleInterval(int daysUntilNextHalloween, int minutesToMidnight) {
        return Math.max(
            1,
            calculateTimeBasedValueMin(
                Config.spawnCycleIntervalNormal,
                Config.spawnCycleIntervalMidnight,
                Config.spawnCycleIntervalHalloween,
                daysUntilNextHalloween,
                minutesToMidnight));
    }

    private int calculateMaxTotalEyesPerDimension(int daysUntilNextHalloween, int minutesToMidnight) {
        return Math.max(
            1,
            calculateTimeBasedValueMax(
                Config.maxTotalEyesPerDimensionNormal,
                Config.maxTotalEyesPerDimensionMidnight,
                Config.maxTotalEyesPerDimensionHalloween,
                daysUntilNextHalloween,
                minutesToMidnight));
    }

    private int calculateMaxEyesAroundPlayer(int daysUntilNextHalloween, int minutesToMidnight) {
        return Math.max(
            1,
            calculateTimeBasedValueMax(
                Config.maxEyesAroundPlayerNormal,
                Config.maxEyesAroundPlayerMidnight,
                Config.maxEyesAroundPlayerHalloween,
                daysUntilNextHalloween,
                minutesToMidnight));
    }

    private int calculateTimeBasedValueMax(int normal, int midnight, int halloween, int daysUntilNextHalloween,
        int minutesToMidnight) {
        int valueByTime = normal + ((midnight - normal) * Math.max(0, 240 - minutesToMidnight)) / 240;
        int valueByDate = normal + ((halloween - normal) * Math.max(0, 30 - daysUntilNextHalloween)) / 30;
        return Math.max(valueByDate, valueByTime);
    }

    private int calculateTimeBasedValueMin(int normal, int midnight, int halloween, int daysUntilNextHalloween,
        int minutesToMidnight) {
        int valueByTime = normal + ((midnight - normal) * Math.max(0, 240 - minutesToMidnight)) / 240;
        int valueByDate = normal + ((halloween - normal) * Math.max(0, 30 - daysUntilNextHalloween)) / 30;
        return Math.min(valueByDate, valueByTime);
    }

    public void spawnOneAround(Vec3 positionVec, EntityPlayer player, float d) {
        float dSqr = d * d;
        int verticalRange = Math.max(8, Math.min(24, (int) d));

        double[] pos = new double[] { 0, 0, 0 };

        for (int i = 0; i < 100; i++) {
            double sX = (1 - 2 * EyesInTheShadows.varInstanceCommon.rand.nextFloat()) * d + positionVec.xCoord;
            double sY = MathHelper.clamp_int(
                (int) (positionVec.yCoord
                    + (1 - 2 * EyesInTheShadows.varInstanceCommon.rand.nextFloat()) * verticalRange),
                1,
                255);
            double sZ = (1 - 2 * EyesInTheShadows.varInstanceCommon.rand.nextFloat()) * d + positionVec.zCoord;

            int spawnY = findValidSpawnY(player.worldObj, (int) sX, (int) sY, (int) sZ, verticalRange);
            if (spawnY < 0) {
                continue;
            }

            pos[0] = sX;
            pos[1] = spawnY;
            pos[2] = sZ;

            double pX = pos[0] + 0.5D;
            double pY = pos[1];
            double pZ = pos[2] + 0.5D;

            double distanceSq = player.getDistanceSq(pX, pY, pZ);
            EntityEyes entity = new EntityEyes(player.worldObj);
            if (distanceSq < dSqr && isValidSpawnSpot(player.worldObj, entity, pos, distanceSq)) {
                // EyesInTheDarkness.EYES.get().create(player.worldObj, null, null, null, pos, MobSpawnType.NATURAL,
                // false, false);
                if (entity == null) continue;

                /*
                 * int canSpawn = net.minecraftforge.common.ForgeHooks. canEntitySpawn(entity, player.worldObj, pX, pY,
                 * pZ, null, MobSpawnType.NATURAL);
                 * if (canSpawn != -1 && (canSpawn == 1 || entity.checkSpawnRules(player.worldObj, MobSpawnType.NATURAL)
                 * && entity.checkSpawnObstruction(player.worldObj)))
                 * {
                 * player.worldObj.addFreshEntity(entity);
                 * return;
                 * }
                 */

                // We are adding 1 to Y so the eyes spawn 1 block above ground. We cant pass Y + 1 to
                // canCreatureTypeSpawnAtLocation because it can only spawn stuff on the ground
                entity.setPosition(pX, pY + 1, pZ);
                EyesInTheShadows.debug("Spawned eyes @ {" + pX + ";" + pY + ";" + pZ + "}");
                player.worldObj.spawnEntityInWorld(entity);

                return;
            }
            entity.setDead();
        }
    }

    private static int findValidSpawnY(World world, int x, int startY, int z, int searchRange) {
        int minY = Math.max(1, startY - searchRange);
        int maxY = Math.min(255, startY + searchRange);

        // Check the sampled Y first, then alternate up/down to stay near player level.
        if (SpawnerAnimals.canCreatureTypeSpawnAtLocation(EnumCreatureType.monster, world, x, startY, z)) {
            return startY;
        }

        for (int delta = 1; delta <= searchRange; delta++) {
            int up = startY + delta;
            if (up <= maxY
                && SpawnerAnimals.canCreatureTypeSpawnAtLocation(EnumCreatureType.monster, world, x, up, z)) {
                return up;
            }

            int down = startY - delta;
            if (down >= minY
                && SpawnerAnimals.canCreatureTypeSpawnAtLocation(EnumCreatureType.monster, world, x, down, z)) {
                return down;
            }
        }

        return -1;
    }

    private static boolean isValidSpawnSpot(World serverWorld, EntityLivingBase entity, double[] pos,
        double sqrDistanceToClosestPlayer) {
        int instantDespawnDistance = 1024; /* EntityLiving, despawnEntity() */
        if (sqrDistanceToClosestPlayer > (instantDespawnDistance * instantDespawnDistance)) {
            return false;
        }

        /*
         * if (!BiomeRules.isBiomeAllowed(serverWorld, serverWorld.getBiome(pos)))
         * {
         * return false;
         * }
         */

        // return SpawnPlacements.checkSpawnRules(entity, serverWorld, MobSpawnType.NATURAL, pos, serverWorld.random)
        // && serverWorld.noCollision(entity.getAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));

        return SpawnerAnimals.canCreatureTypeSpawnAtLocation(
            EnumCreatureType.monster,
            serverWorld,
            (int) pos[0],
            (int) pos[1],
            (int) pos[2]);
    }
}
