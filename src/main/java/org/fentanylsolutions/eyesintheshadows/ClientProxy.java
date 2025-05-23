package org.fentanylsolutions.eyesintheshadows;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.eyesintheshadows.client.EyesRenderer;
import org.fentanylsolutions.eyesintheshadows.client.ModelEyes;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;
import org.fentanylsolutions.eyesintheshadows.event.ClientEventHandler;
import org.fentanylsolutions.eyesintheshadows.varinstances.VarInstanceClient;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {
        EyesInTheShadows.varInstanceClient = new VarInstanceClient();
        EyesInTheShadows.varInstanceClient.preInitHook();

        RenderingRegistry.registerEntityRenderingHandler(EntityEyes.class, new EyesRenderer(new ModelEyes(), 0.5F));

        ClientEventHandler clientEventHandler = new ClientEventHandler();
        MinecraftForge.EVENT_BUS.register(clientEventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(clientEventHandler);
        super.preInit(event);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        EyesInTheShadows.varInstanceClient.postInitHook();
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        super.serverAboutToStart(event);
    }

    // register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }

    public void serverStarted(FMLServerStartedEvent event) {
        super.serverStarted(event);
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        super.serverStopping(event);
    }

    public void serverStopped(FMLServerStoppedEvent event) {
        super.serverStopped(event);
    }

    @Override
    public EntityPlayer getPlayerEntityFromContext(MessageContext ctx) {
        // Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
        // your packets will not work because you will be getting a client
        // player even when you are on the server! Sounds absurd, but it's true.

        // Solution is to double-check side before returning the player:
        return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntityFromContext(ctx));
    }
}
