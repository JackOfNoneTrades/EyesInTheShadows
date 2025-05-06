package org.fentanylsolutions.eyesintheshadows.packet;

import net.minecraft.entity.Entity;

import org.fentanylsolutions.eyesintheshadows.entity.IModEntity;
import org.fentanylsolutions.eyesintheshadows.packet.packets.MessageSyncEntityToClient;

public class PacketUtil {

    public static void sendEntitySyncPacketToClient(IModEntity parEntity) {
        Entity theEntity = (Entity) parEntity;
        if (!theEntity.worldObj.isRemote) {
            // // DEBUG
            // System.out.println("sendEntitySyncPacket from server");
            PacketHandler.net
                .sendToAll(new MessageSyncEntityToClient(theEntity.getEntityId(), parEntity.getSyncDataCompound()));
        }

    }
}
