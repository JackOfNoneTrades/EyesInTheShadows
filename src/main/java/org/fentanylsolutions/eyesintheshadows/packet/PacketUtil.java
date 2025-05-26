package org.fentanylsolutions.eyesintheshadows.packet;

import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;

import org.fentanylsolutions.eyesintheshadows.entity.IModEntity;
import org.fentanylsolutions.eyesintheshadows.packet.packets.MessageSyncEntityToClient;

public class PacketUtil {

    public static void sendEntitySyncPacketToClient(IModEntity parEntity) {
        Entity theEntity = (Entity) parEntity;
        if (!theEntity.worldObj.isRemote) {
            // // DEBUG
            // System.out.println("sendEntitySyncPacket from server");
            // TODO: maybe sync only what is necessary, not everything at the same time
            ((WorldServer) theEntity.worldObj).getEntityTracker()
                .func_151248_b(
                    theEntity,
                    PacketHandler.net.getPacketFrom(
                        new MessageSyncEntityToClient(theEntity.getEntityId(), parEntity.getSyncDataCompound())));
        }
    }
}
