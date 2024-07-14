package com.brov3r.protegon.handlers;

import com.avrix.events.OnAddIncomingEvent;
import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.modules.*;
import com.brov3r.protegon.utils.InventoryTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.nio.ByteBuffer;

/**
 * Handles incoming network events and processes packets accordingly.
 * Extends {@link OnAddIncomingEvent} to override handling method.
 */
public class OnAddIncomingHandler extends OnAddIncomingEvent {
    /**
     * Handles the incoming network event by processing the received packet.
     *
     * @param opcode        the opcode of packet
     * @param data          the ByteBuffer containing packet data
     * @param udpConnection the UDP connection associated with the packet
     */
    @Override
    public void handleEvent(Short opcode, ByteBuffer data, UdpConnection udpConnection) {
        if (data == null || data.limit() == 0) return;

        IsoPlayer player = PlayerUtils.getPlayerByUdpConnection(udpConnection);

        if (player == null) return;

        ZomboidNetData packet;

        if (data.limit() > 2048) {
            packet = ZomboidNetDataPool.instance.getLong(data.limit());
        } else {
            packet = ZomboidNetDataPool.instance.get();
        }

        packet.read(opcode, data, udpConnection);
        data.rewind();

        PacketTypes.PacketType packetType = packet.type;

        if (Main.getConfig().getBoolean("general.isLogPacket")) {
            if (!Main.getConfig().getStringList("general.ignorePacketType").contains(packetType.name())) {
                System.out.printf("[#] Player %s | Packet: %s%n", player.getUsername(), packetType.name());
            }
        }

        try {
            switch (packetType) {
                case ClientCommand -> ClientCommandAC.handlePacket(packet, player, udpConnection);
                case ExtraInfo -> ExtraInfoAC.handlePacket(packet, player, udpConnection);
                case SyncXP -> SkillAC.handlePacket(packet, player, udpConnection);
                case AddXP -> ExpAC.handlePacket(packet, player, udpConnection);
                case SendInventory -> {
                    InventoryTools.updatePlayerInventory(player, packet);
                    InventoryAC.handlePacket(packet, player, udpConnection);
                }
            }
        } catch (Exception e) {
            String exceptionMessage = e.getCause() == null ? e.getMessage() : String.format("%s | %s",
                    e.getMessage(), e.getCause().getMessage());
            System.out.printf("[!] AC - An error occurred while processing package '%s': %s%n", packetType.name(), exceptionMessage);
            e.printStackTrace();
        }

        packet.buffer.rewind();
    }
}