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

        ArrayList<InventoryItem> items = inventory.getItems();
        HashMap<String, Integer> itemsCount = new HashMap<>();

        for (InventoryItem item : items) {
            if (item == null) continue;

            String itemID = item.getFullType().toLowerCase();

            String punishText = String.format("%s | ID: %s",
                    Main.getConfig().getString("itemDupe.punishText"),
                    itemID);

            for (Object idRow : Main.getConfig().getList("itemDupe.blackList")) {
                String blackID = (String) idRow;

                if (item.getFullType().toLowerCase().contains(blackID.toLowerCase())) {
                    AntiCheatUtils.punishPlayer(
                            Main.getConfig().getInt("itemDupe.punishType"),
                            player,
                            punishText);
                    return;
                }
            }

            Integer count = itemsCount.getOrDefault(itemID, 0);
            count++;
            itemsCount.put(itemID, count);
        }

        for (String dataMaxItem : Main.getConfig().getStringList("itemDupe.maxCountItems")) {
            String[] argsData = dataMaxItem.split(":");
            String itemID = argsData[0].toLowerCase().trim();
            int maxCount = 1;
            try {
                maxCount = Integer.parseInt(argsData[1].trim());
            } catch (NumberFormatException numberFormatException) {
                continue;
            }

            Integer count = itemsCount.getOrDefault(itemID, 0);
            String punishText = String.format("%s | ID: %s | Count: %s",
                    Main.getConfig().getString("itemDupe.punishText"),
                    itemID,
                    count);

            if (count >= maxCount) {
                AntiCheatUtils.punishPlayer(
                        Main.getConfig().getInt("itemDupe.punishType"),
                        player,
                        punishText);
                return;
            }
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