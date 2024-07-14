package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

/**
 * Handles extra information packets for anti-cheat purposes.
 */
public class ExtraInfoAC {
    /**
     * Performing anti-cheat actions when receiving a new packet
     *
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("extraInfoAntiCheat.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        AntiCheatUtils.punishPlayer(
                Main.getConfig().getInt("extraInfoAntiCheat.punishType"),
                player,
                Main.getConfig().getString("extraInfoAntiCheat.punishText")
        );
    }
}
