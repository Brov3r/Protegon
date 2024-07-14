package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Handles anti-cheat actions related to vehicle commands.
 */
public class VehicleAC {

    /**
     * Handles vehicle command packets for anti-cheat actions.
     *
     * @param command The command received from the client.
     * @param method  The method associated with the command.
     * @param player  The IsoPlayer who sent the command.
     */
    public static void handleClientCommand(String command, String method, IsoPlayer player) {
        // List of vehicle cheats that trigger anti-cheat actions
        ArrayList<String> vehicleCheats = new ArrayList<>() {{
            add("getKey");
            add("cheatHotwire");
            add("repairPart");
            add("repair");
            add("setRust");
            add("setPartCondition");
        }};

        // Check if vehicle anti-cheat is enabled and the player does not have rights
        if (!Main.getConfig().getBoolean("vehicleAntiCheat.isEnable") || AntiCheatUtils.isPlayerHasRights(player)) {
            return;
        }

        // Check if the method matches any of the vehicle cheats
        for (String cheatCmd : vehicleCheats) {
            if (method.equals(cheatCmd)) {
                // Format the cheat command for punishment message
                String[] words = cheatCmd.split("(?=\\p{Upper})");
                String formattedCmd = Arrays.stream(words)
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));

                // Punish the player for using the detected cheat command
                AntiCheatUtils.punishPlayer(
                        Main.getConfig().getInt("vehicleAntiCheat.punishType"),
                        player,
                        Main.getConfig().getString("vehicleAntiCheat.punishText").replace("<ACTION>", formattedCmd));
                return;
            }
        }
    }
}