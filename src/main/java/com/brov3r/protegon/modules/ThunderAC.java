package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;

/**
 * Thunder
 */
public class ThunderAC {
    /**
     * Handles thunder command packets for anti-cheat actions.
     *
     * @param command The command received from the client.
     * @param method  The method associated with the command.
     * @param player  The IsoPlayer who sent the command.
     */
    public static void handleClientCommand(String command, String method, IsoPlayer player) {
        if (!Main.getConfig().getBoolean("thunder.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        if (command.equalsIgnoreCase("event") && method.equalsIgnoreCase("thunder")) {
            AntiCheatUtils.punishPlayer(
                    Main.getConfig().getInt("thunder.punishType"),
                    player,
                    Main.getConfig().getString("thunder.punishText"));
        }
    }
}