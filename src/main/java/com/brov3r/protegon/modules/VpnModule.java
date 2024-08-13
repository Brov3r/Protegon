package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.ModuleUtils;
import org.json.JSONObject;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * VPN Module
 */
public class VpnModule extends Module {
    /**
     * Constructs a new {@link Module} with the specified packet type, name, and description.
     */
    public VpnModule() {
        super(PacketTypes.PacketType.PlayerConnect, "vpn", "VPN", "Checking connections for VPN");
    }

    /**
     * Handles the processing of a packet received from a {@link UdpConnection}.
     * This method must be implemented by subclasses to define the specific behavior
     * when a packet of the associated type is received.
     *
     * @param buffer     The {@link ByteBuffer} containing the packet data.
     * @param player     The {@link IsoPlayer} object.
     * @param connection The {@link UdpConnection} from which the packet originated.
     */
    @Override
    public void handlePacket(ByteBuffer buffer, IsoPlayer player, UdpConnection connection) {
        String api = Main.getConfig().getString(getConfigName() + ".API").trim();
        String apiUrl = "http://v2.api.iphub.info/ip/" + connection.ip.trim();

        // Check if API key is set to default or empty
        if (api.equalsIgnoreCase("...") || api.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("X-Key", api);

                int responseCode = httpConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) return;

                BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                int shouldBlock = jsonObject.getInt("block");
                String countryCode = jsonObject.getString("countryCode");
                String countryName = jsonObject.getString("countryName");

                String country = countryCode + " - " + countryName;

                System.out.printf("[#] VPN - Checking connection of player '%s' (IP: %s, Country: %s, Ping: %s)%n", connection.username, connection.ip.trim(), country, connection.getAveragePing());

                if (shouldBlock == 1) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignore) {
                    }

                    ModuleUtils.punishPlayer(getConfigName(), connection);
                }
            } catch (Exception ignored) {
            }
        });
    }
}