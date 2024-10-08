package com.brov3r.protegon.utils;

import com.brov3r.protegon.Main;
import org.json.JSONObject;
import zombie.core.raknet.UdpConnection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for sending messages to Discord via webhook.
 */
public class DiscordWebhook {
    /**
     * Sending a message via DiscordWebHook
     *
     * @param udpConnection player connection
     * @param reason        message text
     */
    public static void sendMessage(UdpConnection udpConnection, String adminName, String reason) {
        if (Main.getConfig().getBoolean("discordAlert.isEnable")) return;

        String text = formatTemplate(udpConnection, adminName, reason).trim();
        String webHookUrl = Main.getConfig().getString("discordAlert.webHookURL").trim();
        String avatarURL = Main.getConfig().getString("discordAlert.botAvatarURL").trim();
        String botUsername = Main.getConfig().getString("discordAlert.botUsername").trim();

        try {
            JSONObject json = new JSONObject();
            json.put("content", text);
            json.put("username", botUsername);
            json.put("avatar_url", avatarURL);

            String jsonString = json.toString();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webHookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .join();
        } catch (Exception e) {
            System.out.printf("[!] DiscordAlert - An error occurred while sending a message to Discord: %s%n", e.getMessage());
        }
    }

    /**
     * Formatting a message template for data
     *
     * @param udpConnection player connection
     * @param reason        reason for punishment
     * @return formatted text
     */
    private static String formatTemplate(UdpConnection udpConnection, String adminName, String reason) {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);

        return Main.getConfig().getString("discordAlert.messageTemplate")
                .replace("<PLAYER_NAME>", udpConnection.username)
                .replace("<DATE>", formattedDate)
                .replace("<ADMIN_NAME>", adminName.isEmpty() ? "Console" : adminName)
                .replace("<REASON>", reason.isEmpty() ? "-" : reason);
    }
}