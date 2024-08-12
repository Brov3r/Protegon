package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

import java.nio.ByteBuffer;

/**
 * Handles client commands received from players.
 */
public class ClientCommandAC {
    /**
     * Handles the incoming client command packet.
     *
     * @param packet           The ZomboidNetData packet containing the command data.
     * @param player           The IsoPlayer associated with the command.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        ByteBuffer byteBuffer = packet.buffer;
        byte index = byteBuffer.get();
        String command = zombie.GameWindow.ReadString(byteBuffer);
        String method = zombie.GameWindow.ReadString(byteBuffer);
        boolean isTable = byteBuffer.get() == 1;

        // Log client command if enabled and not "ISLogSystem"
        if (Main.getConfig().getBoolean("general.isLogClientCommand") && !command.equals("ISLogSystem")) {
            System.out.printf("[#] AC - Player '%s' entered the command '%s' with method: %s%n",
                    player.getUsername(), command, method);
        }

        // Delegate handling of vehicle commands
        VehicleAC.handleClientCommand(command, method, player);
        ThunderAC.handleClientCommand(command, method, player);
        FireAC.handleClientCommand(command, method, player);
    }
}