package com.brov3r.protegon.handlers;

import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.ModuleManager;
import com.brov3r.protegon.modules.Module;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.nio.ByteBuffer;

/**
 * The {@code OnAddIncomingHandler} class is responsible for handling incoming network events
 * by processing received packets. It provides functionality to block certain packets and
 * logs packet information if configured.
 */
public class OnAddIncomingHandler {
    /**
     * Flag indicating whether packets should be blocked.
     */
    private static boolean shouldBlock = false;

    /**
     * Sets the flag to block packet processing.
     * When set to true, the handler will block packets from being processed.
     */
    public static void blockPacket() {
        shouldBlock = true;
    }

    /**
     * Should the packet be blocked
     *
     * @return {@code true} if packet processing is blocked, {@code false} otherwise
     */
    public static boolean isShouldBlock() {
        return shouldBlock;
    }

    /**
     * Handles the incoming network event by processing the received packet.
     *
     * <p>This method reads the packet data from the provided {@link ByteBuffer} and processes
     * it using registered {@link Module}s. It also logs packet information if configured.</p>
     *
     * @param opcode        the opcode of the packet
     * @param data          the {@link ByteBuffer} containing packet data
     * @param udpConnection the {@link UdpConnection} associated with the packet
     */
    public static void handlePacket(short opcode, ByteBuffer data, UdpConnection udpConnection) {
        shouldBlock = false;

        if (udpConnection == null) return;

        // Check for empty or null data
        if (data == null || data.limit() == 0) return;

        ZomboidNetData packet;

        // Create a copy of the buffer to isolate data
        ByteBuffer buffer = ByteBuffer.allocate(data.remaining());
        buffer.put(data);
        buffer.flip();

        // Use a pool to obtain a ZomboidNetData instance
        if (buffer.limit() > 2048) {
            packet = ZomboidNetDataPool.instance.getLong(buffer.limit());
        } else {
            packet = ZomboidNetDataPool.instance.get();
        }

        // Read packet data
        packet.read(opcode, buffer, udpConnection);

        PacketTypes.PacketType packetType = packet.type;

        // Log packet details if configured and not in the ignore list
        if (Main.getConfig().getBoolean("general.isLogPacket")
                && !Main.getConfig().getStringList("general.ignorePacketType").contains(packetType.name())) {
            System.out.printf("[#] Player %s > Packet: %s%n", udpConnection.username, packetType.name());
        }

        try {
            // Process the packet with each registered module
            for (Module module : ModuleManager.getModules()) {
                if (packetType != module.getPacketType()) continue;

                if (module.isIgnorePacket(PlayerUtils.getPlayerByUdpConnection(udpConnection))) continue;

                module.handlePacket(packet.buffer, PlayerUtils.getPlayerByUdpConnection(udpConnection), udpConnection);

                // Rewind the buffer for reuse
                packet.buffer.rewind();
            }
        } catch (Exception e) {
            // Format exception message for logging
            String exceptionMessage = e.getCause() == null ? e.getMessage() : String.format("%s | %s",
                    e.getMessage(), e.getCause().getMessage());
            System.out.printf("[!] PacketHandler - An error occurred while processing packet '%s': %s%n", packetType.name(), exceptionMessage);

            packet.buffer.rewind();
        }
    }
}