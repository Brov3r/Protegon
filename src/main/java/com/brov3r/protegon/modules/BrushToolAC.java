package com.brov3r.protegon.modules;

import com.avrix.utils.IsoObjectUtils;
import com.avrix.utils.PlayerUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.WorldItemTypes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Anti-Brush Tool functionality for placing game objects.
 */
public class BrushToolAC {

    /**
     * Map to store placement data for each player.
     */
    private static final Map<String, PlacementData> placementMap = new HashMap<>();

    /**
     * Inner class to store placement data.
     */
    private static class PlacementData {
        String lastObjectName;
        long lastPlaceTime;
        int objectCount;
        int totalObjectCount;

        PlacementData(String lastObjectName, long lastPlaceTime, int objectCount, int totalObjectCount) {
            this.lastObjectName = lastObjectName;
            this.lastPlaceTime = lastPlaceTime;
            this.objectCount = objectCount;
            this.totalObjectCount = totalObjectCount;
        }
    }

    /**
     * Handles the packet for placing game objects and enforces anti-brush tool measures.
     *
     * @param buffer     The ByteBuffer containing packet data.
     * @param connection The UdpConnection from which the packet originated.
     * @return true if the packet handling was successful; false otherwise.
     */
    public static boolean handlePacket(ByteBuffer buffer, UdpConnection connection) {
        if (connection == null) return false;

        IsoPlayer player = PlayerUtils.getPlayerByUdpConnection(connection);

        if (player == null) return false;

        // Check if anti-brush tool is enabled and if player has rights to bypass it
        if (!Main.getConfig().getBoolean("antiBrushTool.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return true;

        IsoObject object = WorldItemTypes.createFromBuffer(buffer);

        try {
            object.load(buffer, 195);
        } catch (IOException e) {
            System.out.println("[!] AC - Error loading package data to generate an object for installation on the map! Player from packet: " + connection.username);
            return true;
        }

        int x = buffer.getInt();
        int y = buffer.getInt();
        int z = buffer.getInt();

        object.square = IsoWorld.instance.CurrentCell.getGridSquare(x, y, z);

        if (object.square == null) return true;

        object.removeFromSquare();
        object.removeFromWorld();
        object.removeAllContainers();

        String objectName = object.getName() != null ? object.getName() : object.getObjectName();
        boolean isInventoryObject = objectName.equals("WorldInventoryItem");
        String spriteName = object.getSprite() != null ? object.getSprite().getName() : "";

        if (object.getSprite() != null && object.getSprite().getName() != null) {
            objectName = objectName + " (" + spriteName + ")";
        }

        if (isInventoryObject) return true;

        // Check against whitelist sprites
        for (String whiteListSprite : Main.getConfig().getStringList("antiBrushTool.whiteListSprite")) {
            if (spriteName.toLowerCase().contains(whiteListSprite.toLowerCase())) return true;
            if (objectName.toLowerCase().contains(whiteListSprite.toLowerCase())) return true;
        }

        // Check against blacklist sprites
        for (String blackListSprite : Main.getConfig().getStringList("antiBrushTool.blackListSprite")) {
            if (blackListSprite == null || blackListSprite.isEmpty()) continue;

            if (spriteName.equalsIgnoreCase(blackListSprite)) {
                String punishText = Main.getConfig().getString("antiBrushTool.punishText")
                        .replace("<DISTANCE>", "0.0")
                        .replace("<SPRITE>", objectName);

                AntiCheatUtils.punishPlayer(Main.getConfig().getInt("antiBrushTool.punishType"),
                        player,
                        punishText);
                return false;
            }
        }

        // Handle placement limits and checks
        float distance = IsoObjectUtils.getDistance(player, object);

        System.out.printf("[#] AC - Player '%s' placed object '%s' at coordinates: [%s, %s, %s]. Distance: %.1f%n",
                player.getUsername(),
                objectName,
                object.getX(),
                object.getY(),
                object.getZ(),
                distance);

        String punishText = Main.getConfig().getString("antiBrushTool.punishText")
                .replace("<DISTANCE>", String.format("%.1f", distance))
                .replace("<SPRITE>", objectName);

        long currentTime = System.currentTimeMillis();
        String playerUsername = player.getUsername();
        PlacementData placeData = placementMap.getOrDefault(playerUsername, new PlacementData("", 0, 0, 0));

        if (!placeData.lastObjectName.equals(objectName)) {
            placeData.objectCount = 0;
            placeData.totalObjectCount = 0;
        }

        if (currentTime - placeData.lastPlaceTime <= Main.getConfig().getInt("antiBrushTool.placeTimeLimit")) {
            placeData.objectCount++;
        } else {
            placeData.objectCount = 1;
        }

        int maxTotalObjectDelay = Main.getConfig().getInt("antiBrushTool.maxTotalTimeDelay");
        if (currentTime - placeData.lastPlaceTime > maxTotalObjectDelay) {
            placeData.totalObjectCount = 0;
        }

        placeData.totalObjectCount++;
        placeData.lastPlaceTime = currentTime;
        placeData.lastObjectName = objectName;
        placementMap.put(playerUsername, placeData);

        boolean isFastPlace = placeData.objectCount > Main.getConfig().getInt("antiBrushTool.maxObjectPlace");
        int maxTotalObjectCount = Main.getConfig().getInt("antiBrushTool.maxTotalObjectPlace");

        boolean isExceedingTotalLimit = placeData.totalObjectCount > maxTotalObjectCount && (currentTime - placeData.lastPlaceTime) <= maxTotalObjectDelay;

        if (isFastPlace || isExceedingTotalLimit) {
            AntiCheatUtils.punishPlayer(Main.getConfig().getInt("antiBrushTool.punishType"),
                    player,
                    punishText);

            return false;
        }

        return true;
    }
}