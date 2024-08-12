package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoPlayer;
import zombie.chat.ChatBase;
import zombie.chat.ChatMessage;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.chat.ChatServer;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Chat impersonator
 */
public class ChatImpersonatorAC {
    /**
     * Handles the chat impersonator packet for anti-cheat actions.
     *
     * @param packet           The ZomboidNetData packet containing skill information.
     * @param player           The IsoPlayer who sent the packet.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("chatImpersonator.isEnable") || AntiCheatUtils.isPlayerHasRights(player))
            return;

        int chatID = packet.buffer.getInt();

        try {
            Field chatsField = ChatServer.class.getDeclaredField("chats");
            chatsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Integer, ChatBase> chats = (Map<Integer, ChatBase>) chatsField.get(null);

            synchronized (chats) {
                ChatBase chatBase = chats.get(chatID);
                if (chatBase != null) {
                    ChatMessage chatMessage = chatBase.unpackMessage(packet.buffer);
                    if (chatMessage.getAuthor().equalsIgnoreCase(player.getUsername())) return;

                    AntiCheatUtils.punishPlayer(
                            Main.getConfig().getInt("chatImpersonator.punishType"),
                            player,
                            Main.getConfig().getString("chatImpersonator.punishText"));
                }
            }
        } catch (Exception e) {
            System.out.println("[!] Error processing player message:" + e.getMessage());
        }
    }
}