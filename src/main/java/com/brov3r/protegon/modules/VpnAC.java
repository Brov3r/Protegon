package com.brov3r.protegon.modules;

import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import org.json.JSONObject;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * Handles anti-cheat actions related to detecting VPN usage by players.
 */
public class VpnAC {

    /**
     * Handles the packet to detect VPN usage by player IP address.
     *
     * @param playerConnection The UDP connection of the player.
     */
    public static void handlePacket(UdpConnection playerConnection) {
        IsoPlayer player = PlayerUtils.getPlayerByUdpConnection(playerConnection);

        if (player == null) return;

        // Check if VPN detection is enabled and the player does not have rights
        if (!Main.getConfig().getBoolean("antiVPN.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        String playerIP = playerConnection.ip.trim();
        String api = Main.getConfig().getString("antiVPN.API").trim();
        String apiUrl = "http://v2.api.iphub.info/ip/" + playerIP;

        // Check if API key is set to default or empty
        if (api.equalsIgnoreCase("...") || api.isEmpty()) return;

        System.out.printf("[#] AC - Checking player '%s' for VPN use by IP address: %s%n", playerConnection.username, playerIP);

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Key", api);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) return;

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                int shouldBlock = jsonObject.getInt("block");

                if (shouldBlock == 1) {
                    AntiCheatUtils.punishPlayer(Main.getConfig().getInt("antiVPN.punishType"),
                            player,
                            Main.getConfig().getString("antiVPN.punishText"));
                } else {
                    System.out.printf("[#] AC - Player '%s' has been verified and no VPN was detected!%n", playerConnection.username);
                }
            } catch (Exception ignored) {
            }
        });
    }
}