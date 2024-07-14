package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import com.brov3r.protegon.utils.InventoryTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.network.GameServer;
import zombie.network.ZomboidNetData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InventoryAC {
    /**
     * Performing anti-cheat actions when receiving a new packet update Inventory
     *
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("itemDupe.isEnable") || AntiCheatUtils.isPlayerHasRights(player)) return;

        ItemContainer inventory = player.getInventory();
        if (inventory == null) return;

        HashMap<String, Integer> itemsCount = new HashMap<>();

        for (InventoryItem item : inventory.getItems()) {
            if (item == null) continue;

            itemsCount.merge(item.getFullType(), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : itemsCount.entrySet()) {
            String itemID = entry.getKey();
            int count = entry.getValue();

            for (String blackID : Main.getConfig().getStringList("itemDupe.blackList")) {
                if (itemID.toLowerCase().contains(blackID.toLowerCase())) {
                    AntiCheatUtils.punishPlayer(
                            Main.getConfig().getInt("itemDupe.punishType"),
                            player,
                            Main.getConfig().getString("itemDupe.punishTextDupe").replace("<ITEM>", itemID));
                    return;
                }
            }
        }
    }

    /**
     * Checking a player for unlimited carry
     *
     * @param player    player instance
     * @param weight    player's current weight
     * @param maxWeight maximum player weight
     */
    public static void handleInvWeight(IsoPlayer player, float weight, float maxWeight) {
        if (!Main.getConfig().getBoolean("itemDupe.isEnable") || AntiCheatUtils.isPlayerHasRights(player)) return;

        int limitWeight = Main.getConfig().getInt("itemDupe.playerMaxWeight");

        if (weight >= limitWeight) {
            AntiCheatUtils.punishPlayer(
                    Main.getConfig().getInt("itemDupe.punishType"),
                    player,
                    Main.getConfig().getString("itemDupe.punishTextUnlimitedCarry").replace("<WEIGHT>", String.valueOf(weight)));
        }
    }

    /**
     * Requesting inventory from players
     */
    public static void updatePlayersInventory() {
        if (GameServer.udpEngine == null) return;

        ArrayList<IsoPlayer> players = GameServer.getPlayers();
        if (players.isEmpty()) return;

        for (IsoPlayer player : players) {
            UdpConnection playerConnection = GameServer.getConnectionFromPlayer(player);
            if (playerConnection == null) continue;

            InventoryTools.requestPlayerInventory(playerConnection);
        }
    }

    /**
     * Initializing periodic updating of players' inventory (synchronization)
     */
    public static void initInventoryUpdate() {
        if (!Main.getConfig().getBoolean("itemDupe.isEnable")) return;

        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
        schedule.scheduleAtFixedRate(InventoryAC::updatePlayersInventory,
                10,
                Main.getConfig().getInt("itemDupe.updateTime"),
                TimeUnit.MILLISECONDS);
    }
}