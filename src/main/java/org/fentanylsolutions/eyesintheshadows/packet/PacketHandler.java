package org.fentanylsolutions.eyesintheshadows.packet;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.packet.packets.InitiateJumpscarePacket;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper net;

    public static void initPackets() {
        net = NetworkRegistry.INSTANCE.newSimpleChannel(EyesInTheShadows.MODID.toUpperCase());
        registerMessage(InitiateJumpscarePacket.class, InitiateJumpscarePacket.SimpleMessage.class);
    }

    private static int nextPacketId = 0;

    private static void registerMessage(Class packet, Class message) {
        net.registerMessage(packet, message, nextPacketId, Side.CLIENT);
        nextPacketId++;
    }
}
