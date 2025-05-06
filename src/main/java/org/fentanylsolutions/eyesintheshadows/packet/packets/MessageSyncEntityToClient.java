/*
 * Based on:
 * https://github.com/jabelar/WildAnimalsPlus-1.7.10/blob/master/src/main/java/com/blogspot/jabelarminecraft/wildanimals
 * /networking/MessageSyncEntityToClient.java
 */
/* Now you know what it is based on */

package org.fentanylsolutions.eyesintheshadows.packet.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.entity.IModEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageSyncEntityToClient implements IMessage {

    private int entityId;
    private NBTTagCompound entitySyncDataCompound;

    @SuppressWarnings("unused")
    public MessageSyncEntityToClient() {}

    public MessageSyncEntityToClient(int parEntityId, NBTTagCompound parTagCompound) {
        entityId = parEntityId;
        entitySyncDataCompound = parTagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = ByteBufUtils.readVarInt(buf, 4);
        entitySyncDataCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, entityId, 4);
        ByteBufUtils.writeTag(buf, entitySyncDataCompound);
    }

    public static class Handler implements IMessageHandler<MessageSyncEntityToClient, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncEntityToClient message, MessageContext ctx) {
            EntityPlayer thePlayer = EyesInTheShadows.proxy.getPlayerEntityFromContext(ctx);
            IModEntity theEntity = (IModEntity) thePlayer.worldObj.getEntityByID(message.entityId);
            if (theEntity != null) {
                theEntity.setSyncDataCompound(message.entitySyncDataCompound);
            } else {
                EyesInTheShadows.debug("Can't find entity with ID = " + message.entityId + " on client");
            }
            return null;
        }
    }
}
