package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.network.ZomboidNetData;

/**
 * Stats
 */
public class ChangeStatsAC {
    /**
     * Handles the stats packet for anti-cheat actions.
     *
     * @param packet           The ZomboidNetData packet containing skill information.
     * @param player           The IsoPlayer who sent the packet.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("changeStats.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        short targetID = packet.buffer.getShort();

        IsoPlayer targetPlayer = GameServer.IDToPlayerMap.get(targetID);

        if (targetPlayer.getUsername().equalsIgnoreCase(player.getUsername())) return;

        AntiCheatUtils.punishPlayer(
                Main.getConfig().getInt("changeStats.punishType"),
                player,
                Main.getConfig().getString("changeStats.punishText"));
    }
}