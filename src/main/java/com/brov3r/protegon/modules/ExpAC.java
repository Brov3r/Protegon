package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.packets.PlayerID;
import zombie.network.packets.hit.Perk;

import java.nio.ByteBuffer;

/**
 * Handles experience gain packets for players.
 */
public class ExpAC {
    /**
     * Handles the packet containing experience gain information.
     *
     * @param packet           The ZomboidNetData packet containing experience data.
     * @param player           The IsoPlayer who gained experience.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("skillAntiCheat.isEnable")
                || !Main.getConfig().getBoolean("skillAntiCheat.isLogChangeExperience")) {
            return;
        }

        ByteBuffer byteBuffer = packet.buffer;
        PlayerID target = new PlayerID();
        target.parse(byteBuffer, playerConnection);
        target.parsePlayer(playerConnection);
        Perk perk = new Perk();
        perk.parse(byteBuffer, playerConnection);
        int amount = byteBuffer.getInt();

        System.out.printf("[#] AC - Player '%s' gained %s experience in skill '%s'%n", player.getUsername(), amount, perk.getPerk().getId());
    }
}