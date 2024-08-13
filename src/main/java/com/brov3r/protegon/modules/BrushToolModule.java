package com.brov3r.protegon.modules;

import com.avrix.utils.IsoObjectUtils;
import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.PacketTypes;
import zombie.network.WorldItemTypes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * BrushTool
 */
public class BrushToolModule extends Module {
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

        PlacementData(String lastObjectName, long lastPlaceTime) {
            this.lastObjectName = lastObjectName;
            this.lastPlaceTime = lastPlaceTime;
        }
    }

    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public BrushToolModule() {
        super(PacketTypes.PacketType.AddItemToMap, "brushTool", "Brush Tool", "Blocking the use of BrushTool");
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
        if (player == null) return;

        IsoObject object = WorldItemTypes.createFromBuffer(buffer);

        if (object == null) {
            System.out.println("[!] BrushTool - Error: Failed to create object from buffer for player: " + connection.username);
            return;
        }

        try {
            object.load(buffer, 195);
        } catch (IOException e) {
            System.out.println("[!] BrushTool - Error loading package data to generate an object for installation on the map! Player from packet: " + connection.username);
            return;
        }

        int x = buffer.getInt();
        int y = buffer.getInt();
        int z = buffer.getInt();

        object.square = IsoWorld.instance.CurrentCell.getGridSquare(x, y, z);

        if (object.square == null) {
            System.out.println("[!] BrushTool - Error: Grid square not found for object placement for player: " + connection.username);
            return;
        }

        float distance = IsoObjectUtils.getDistance(player, object);

        String objectName = object.getName() != null ? object.getName() : object.getObjectName();
        boolean isInventoryObject = objectName.equals("WorldInventoryItem");
        String spriteName = object.getSprite() != null ? object.getSprite().getName() : "";

        if (object.getSprite() != null && object.getSprite().getName() != null) {
            objectName = objectName + " (" + spriteName + ")";
        }

        if (isInventoryObject) return;

        // Check against whitelist sprites
        for (String whiteListSprite : Main.getConfig().getStringList(getConfigName() + ".whiteListSprite")) {
            if (spriteName.toLowerCase().contains(whiteListSprite.toLowerCase())) return;
            if (objectName.toLowerCase().contains(whiteListSprite.toLowerCase())) return;
        }

        // Check against blacklist sprites
        for (String blackListSprite : Main.getConfig().getStringList(getConfigName() + ".blackListSprite")) {
            if (blackListSprite == null || blackListSprite.isEmpty()) continue;

            if (spriteName.equalsIgnoreCase(blackListSprite)) {
                object.removeFromSquare();
                object.removeFromWorld();
                object.removeAllContainers();
                ModuleUtils.punishPlayer(getConfigName(), connection);
                return;
            }
        }

        // Handle placement limits and checks
        System.out.printf("[#] BrushTool - Player '%s' placed object '%s' at coordinates: [%s, %s, %s]. Distance: %.1f%n",
                player.getUsername(),
                objectName,
                object.getX(),
                object.getY(),
                object.getZ(),
                distance);

        long currentTime = System.currentTimeMillis();
        String playerUsername = player.getUsername();
        PlacementData placeData = placementMap.getOrDefault(playerUsername, new PlacementData("", 0));

        long deltaPlaceTime = currentTime - placeData.lastPlaceTime;

        int placeDistanceMin = Main.getConfig().getInt(getConfigName() + ".placeDistanceMin");
        int placeDistanceMax = Main.getConfig().getInt(getConfigName() + ".placeDistanceMax");
        int placeTimeMin = Main.getConfig().getInt(getConfigName() + ".placeTimeMin");
        int placeTimeMax = Main.getConfig().getInt(getConfigName() + ".placeTimeMax");

        if ((deltaPlaceTime > placeTimeMin && deltaPlaceTime < placeTimeMax) || (distance > placeDistanceMin && distance < placeDistanceMax)) {
            object.removeFromSquare();
            object.removeFromWorld();
            object.removeAllContainers();
            ModuleUtils.punishPlayer(getConfigName(), connection);
            return;
        }

        placeData.lastPlaceTime = currentTime;
        placeData.lastObjectName = objectName;
        placementMap.put(playerUsername, placeData);
    }
}