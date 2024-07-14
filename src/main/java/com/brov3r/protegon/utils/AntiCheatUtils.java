package com.brov3r.protegon.utils;

import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.enums.PunishType;
import zombie.characters.IsoPlayer;

/**
 * Utility class for handling anti-cheat actions.
 */
public class AntiCheatUtils {
    /**
     * Punishes the player according to the specified punishment type.
     *
     * @param punishType The punishment type, represented as an integer value.
     *                   Must match the ordinal value of the {@link PunishType} enumeration.
     * @param player     The player ({@link IsoPlayer}) to whom the penalty is assigned.
     * @param reason     The reason for the penalty, represented as a string.
     */
    public static void punishPlayer(int punishType, IsoPlayer player, String reason) {
        if (punishType == PunishType.NOTHING.ordinal()) return;

        System.out.printf("[!] AC - Player '%s' was found to be in violation due to: %s%n", player.getDisplayName(), reason);

        if (punishType == PunishType.LOGGING.ordinal()) return;

        if (punishType == PunishType.KICK.ordinal()) {
            PlayerUtils.kickPlayer(player, reason);
            return;
        }

        if (punishType == PunishType.BAN.ordinal()) {
            PlayerUtils.banPlayer(player,
                    reason,
                    Main.getConfig().getBoolean("general.isBanIpEnable"),
                    Main.getConfig().getBoolean("general.isBanSteamIdEnable"));
        }
    }

    /**
     * Checks if the player has rights to ignore anti-cheat
     *
     * @param player The player whose package is being checked.
     * @return true if the package is authorized, false otherwise.
     */
    public static boolean isPlayerHasRights(IsoPlayer player) {
        for (String userName : Main.getConfig().getStringList("general.whiteListUsername")) {
            if (userName == null || userName.isEmpty()) continue;

            if (userName.equalsIgnoreCase(player.getUsername())) return true;
        }

        for (String group : Main.getConfig().getStringList("general.whiteListGroup")) {
            if (group == null || group.isEmpty()) continue;

            if (group.equalsIgnoreCase(player.getAccessLevel())) return true;
        }

        return false;
    }
}