package org.fentanylsolutions.eyesintheshadows.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;

import org.fentanylsolutions.eyesintheshadows.Config;
import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class ConfigGui extends GuiConfig {

    private static IConfigElement ceGeneral = new ConfigElement(Config.config.getCategory(Config.Categories.general));
    private static IConfigElement ceAggro = new ConfigElement(
        Config.config.getCategory(Config.Categories.eye_aggression));
    private static IConfigElement ceSpawning = new ConfigElement(Config.config.getCategory(Config.Categories.spawning));
    private static IConfigElement ceMisc = new ConfigElement(Config.config.getCategory(Config.Categories.misc));
    private static IConfigElement cePotion = new ConfigElement(Config.config.getCategory(Config.Categories.potion));
    private static IConfigElement ceMobInteractions = new ConfigElement(
        Config.config.getCategory(Config.Categories.mob_interactions));
    private static IConfigElement ceVisual = new ConfigElement(Config.config.getCategory(Config.Categories.visual));
    private static IConfigElement ceSounds = new ConfigElement(
        Config.config.getCategory(Config.Categories.sound_volumes));

    public ConfigGui(GuiScreen parent) {
        // this.parentScreen = parent;
        super(
            parent,
            ImmutableList.of(ceGeneral, ceAggro, ceSpawning, cePotion, ceMobInteractions, ceVisual, ceSounds, ceMisc),
            EyesInTheShadows.MODID,
            EyesInTheShadows.MODID,
            false,
            false,
            I18n.format(EyesInTheShadows.MODID + ".configgui.title"),
            EyesInTheShadows.confFile.getAbsolutePath());
        EyesInTheShadows.debug("Instantiating config gui");
    }

    @Override
    public void initGui() {
        // You can add buttons and initialize fields here
        super.initGui();
        EyesInTheShadows.debug("Initializing config gui");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // You can do things like create animations, draw additional elements, etc. here
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        EyesInTheShadows.debug("Config button id " + b.id + " pressed");
        super.actionPerformed(b);
        /* "Done" button */
        if (b.id == 2000) {
            /* Syncing config */
            Config.synchronizeConfigurationClient(EyesInTheShadows.confFile, true, false);
        }
    }

}
