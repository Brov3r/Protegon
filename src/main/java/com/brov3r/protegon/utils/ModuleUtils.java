package com.brov3r.protegon.utils;

import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.enums.PunishType;
import com.brov3r.protegon.handlers.OnAddIncomingHandler;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;

/**
 * Utility class for handling anti-cheat actions.
 */
public class ModuleUtils {
    /**
     * Punishes the player according to the specified punishment type.
     *
     * @param moduleName Module name from config
     * @param connection The player ({@link UdpConnection}) to whom the penalty is assigned.
     */
    public static void punishPlayer(String moduleName, UdpConnection connection) {
        int punishType = Main.getConfig().getInt(moduleName + ".punishType");
        String reason = Main.getConfig().getString(moduleName + ".punishText");

        if (punishType == PunishType.NOTHING.ordinal()) return;

        System.out.printf("[?] AC - Player '%s' (IP: %s, SteamID: %s) was seen in: %s%n", connection.username, connection.ip, connection.steamID, reason);

        if (punishType == PunishType.LOGGING.ordinal()) return;

        if (punishType == PunishType.KICK.ordinal()) {
            OnAddIncomingHandler.blockPacket();
            PlayerUtils.kickPlayer(connection, reason);
            return;
        }

        if (punishType == PunishType.BAN.ordinal()) {
            OnAddIncomingHandler.blockPacket();
            PlayerUtils.banPlayer(connection,
                    reason,
                    Main.getConfig().getBoolean("general.isBanIpEnable"),
                    Main.getConfig().getBoolean("general.isBanSteamIdEnable"));
        }
    }

    /**
     * Checks whether a player has rights based on the configured whitelist of usernames and groups.
     *
     * <p>This method checks if the given player is listed in the whitelist by either their username or their access level (group).
     * The whitelisted usernames and groups are retrieved from the configuration file.</p>
     *
     * @param player The {@link IsoPlayer} object representing the player whose rights are being checked.
     * @return {@code true} if the player is listed in the whitelist by username or group; {@code false} otherwise.
     */
    public static boolean isPlayerHasRights(IsoPlayer player) {
        if (player == null) return false;

        for (String userName : Main.getConfig().getStringList("general.whiteListUsername")) {
            if (userName == null || userName.isEmpty()) continue;

            if (userName.equalsIgnoreCase(player.username)) return true;
        }

        for (String group : Main.getConfig().getStringList("general.whiteListGroup")) {
            if (group == null || group.isEmpty()) continue;

            if (group.equalsIgnoreCase(player.getAccessLevel())) return true;
        }

        return false;
    }
}