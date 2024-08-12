package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.packets.SyncSafehousePacket;

/***
 * Safehouse
 */
public class SafeHouseAC {
    /**
     * Handles the safehouse packet for anti-cheat actions.
     *
     * @param packet           The ZomboidNetData packet containing skill information.
     * @param player           The IsoPlayer who sent the packet.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("safeHouse.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        SyncSafehousePacket safehousePacket = new SyncSafehousePacket();
        safehousePacket.parse(packet.buffer, playerConnection);
        if (safehousePacket.validate(playerConnection)) {
            if (safehousePacket.ownerUsername.equalsIgnoreCase(player.getUsername())) return;

            AntiCheatUtils.punishPlayer(
                    Main.getConfig().getInt("safeHouse.punishType"),
                    player,
                    Main.getConfig().getString("safeHouse.punishText"));
        }
    }
}