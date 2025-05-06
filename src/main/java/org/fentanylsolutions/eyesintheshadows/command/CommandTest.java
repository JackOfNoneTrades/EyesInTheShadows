package org.fentanylsolutions.eyesintheshadows.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

import cpw.mods.fml.common.FMLCommonHandler;

public class CommandTest implements ICommand {

    public static boolean isOp(EntityPlayerMP entityPlayerMP) {
        // func_152596_g: canSendCommands
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance()
            .getConfigurationManager()
            .func_152596_g(entityPlayerMP.getGameProfile());
    }

    private final List aliases;

    public CommandTest() {
        aliases = new ArrayList();
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "tabfaces_test";
    }

    @Override
    public String getCommandUsage(ICommandSender var1) {
        return "/tabfaces_test";
    }

    @Override
    public List getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] argString) {
        if (sender instanceof EntityPlayerMP && !isOp((EntityPlayerMP) sender)) {
            sender
                .addChatMessage(new ChatComponentText((char) 167 + "cYou do not have permission to use this command"));
            return;
        }

        sender.addChatMessage(new ChatComponentText("Bruh"));

        if (argString.length == 0) {

        } else {

        }

        EyesInTheShadows.LOG.info(sender.getCommandSenderName() + " issued test command");
        EyesInTheShadows.proxy.eyesSpawningManager.spawnOneAround(
            Vec3.createVectorHelper(
                sender.getPlayerCoordinates().posX,
                sender.getPlayerCoordinates().posY,
                sender.getPlayerCoordinates().posZ),
            sender.getEntityWorld()
                .getPlayerEntityByName(sender.getCommandSenderName()),
            10);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender var1) {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender var1, String[] var2) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] var1, int var2) {
        return false;
    }
}
