package org.fentanylsolutions.eyesintheshadows;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.eyesintheshadows.command.CommandTest;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.fentanylsolutions.eyesintheshadows.event.CommonEventHandler;
import org.fentanylsolutions.eyesintheshadows.packet.PacketHandler;
import org.fentanylsolutions.eyesintheshadows.util.MobUtil;
import org.fentanylsolutions.eyesintheshadows.util.PotionUtil;
import org.fentanylsolutions.eyesintheshadows.util.Util;
import org.fentanylsolutions.eyesintheshadows.varinstances.VarInstanceCommon;
import org.fentanylsolutions.eyesintheshadows.varinstances.VarInstanceServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.EntityRegistry;

public class CommonProxy {

    protected int modEntityID = -1;
    public EyesSpawningManager eyesSpawningManager;

    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {
        EyesInTheShadows.varInstanceCommon = new VarInstanceCommon();
        if (Util.isServer()) {
            EyesInTheShadows.varInstanceServer = new VarInstanceServer();
        }

        EyesInTheShadows.confFile = event.getSuggestedConfigurationFile();
        if (Util.isServer()) {
            Config.synchronizeConfigurationServer(event.getSuggestedConfigurationFile(), false);
        } else {
            Config.synchronizeConfigurationClient(event.getSuggestedConfigurationFile(), false, true);
        }
        EyesInTheShadows.debug("I am " + EyesInTheShadows.MODID + " at version " + Tags.VERSION);

        PacketHandler.initPackets();

        EntityRegistry
            .registerModEntity(EntityEyes.class, "Eyes", ++modEntityID, EyesInTheShadows.instance, 80, 3, false);
        Util.registerSpawnEgg("Eyes", 0x000000, 0x7F0000);

        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event) {
        if (Config.printPotions) {
            PotionUtil.printPotionNames();
        }
        if (Config.printMobs) {
            MobUtil.printMobNames();
        }
        EyesInTheShadows.varInstanceCommon.postInitHook();

        eyesSpawningManager = new EyesSpawningManager();
        MinecraftForge.EVENT_BUS.register(eyesSpawningManager);
        FMLCommonHandler.instance()
            .bus()
            .register(eyesSpawningManager);
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

    }

    // register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {
        //if (Util.isServer()) {
            if (EyesInTheShadows.isDebugMode()) {
                event.registerServerCommand(new CommandTest());
            }
        //}
    }

    public void serverStarted(FMLServerStartedEvent event) {

    }

    public void serverStopping(FMLServerStoppingEvent event) {

    }

    public void serverStopped(FMLServerStoppedEvent event) {

    }

    public EntityPlayer getPlayerEntityFromContext(MessageContext ctx) {
        return ctx.getServerHandler().playerEntity;
    }
}
