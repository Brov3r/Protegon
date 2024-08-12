package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;

import java.util.ArrayList;

/**
 * Fire
 */
public class FireAC {
    public static ArrayList<String> fireCheats = new ArrayList<>() {{
        add("addFireOnSquare");
        add("addSmokeOnSquare");
        add("addExplosionOnSquare");
    }};

    /**
     * Handles fire packets for anti-cheat actions.
     *
     * @param command The command received from the client.
     * @param method  The method associated with the command.
     * @param player  The IsoPlayer who sent the command.
     */
    public static void handleClientCommand(String command, String method, IsoPlayer player) {
        if (!Main.getConfig().getBoolean("fire.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        if (command.equalsIgnoreCase("object")) {

            for (String cheatCmd : fireCheats) {
                if (method.equalsIgnoreCase(cheatCmd)) {
                    AntiCheatUtils.punishPlayer(
                            Main.getConfig().getInt("fire.punishType"),
                            player,
                            Main.getConfig().getString("fire.punishText"));
                    return;
                }
            }
        }
    }
}