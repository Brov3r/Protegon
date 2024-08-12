package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

/**
 * Explosive
 */
public class ExplosiveAC {
    /**
     * Handles the explosive packet for anti-cheat actions.
     *
     * @param packet           The ZomboidNetData packet containing skill information.
     * @param player           The IsoPlayer who sent the packet.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("explosive.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        AntiCheatUtils.punishPlayer(
                Main.getConfig().getInt("explosive.punishType"),
                player,
                Main.getConfig().getString("explosive.punishText"));
    }
}
