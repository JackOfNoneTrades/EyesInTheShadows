package org.fentanylsolutions.eyesintheshadows;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fentanylsolutions.eyesintheshadows.varinstances.VarInstanceClient;
import org.fentanylsolutions.eyesintheshadows.varinstances.VarInstanceCommon;
import org.fentanylsolutions.eyesintheshadows.varinstances.VarInstanceServer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = EyesInTheShadows.MODID, version = Tags.VERSION, name = "MyMod", acceptedMinecraftVersions = "[1.7.10]")
public class EyesInTheShadows {

    public static final String MODID = "eyesintheshadows";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static EyesInTheShadows instance;

    public static VarInstanceClient varInstanceClient;
    public static VarInstanceServer varInstanceServer;
    public static VarInstanceCommon varInstanceCommon;
    private static boolean DEBUG_MODE;
    public static String rootPath = MODID;
    public static final int maxPngDimension = 2500;
    public static File confFile;

    @SidedProxy(
        clientSide = "org.fentanylsolutions.eyesintheshadows.ClientProxy",
        serverSide = "org.fentanylsolutions.eyesintheshadows.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        String debugVar = System.getenv("MCMODDING_DEBUG_MODE");
        DEBUG_MODE = debugVar != null;
        EyesInTheShadows.LOG.info("Debugmode: " + DEBUG_MODE);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    public static boolean isDebugMode() {
        return DEBUG_MODE || Config.debugMode;
    }

    public static void debug(String message) {
        if (isDebugMode()) {
            LOG.info("DEBUG: " + message);
        }
    }
}
