package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.chat.ChatBase;
import zombie.chat.ChatMessage;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;
import zombie.network.chat.ChatServer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Chat Impersonator
 */
public class ChatImpersonatorModule extends Module {
    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public ChatImpersonatorModule() {
        super(PacketTypes.PacketType.ChatMessageFromPlayer, "chatImpersonator", "Chat Impersonator", "Blocking the ability to send messages on behalf of someone else");
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
        int chatID = buffer.getInt();

        try {
            Field chatsField = ChatServer.class.getDeclaredField("chats");
            chatsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Integer, ChatBase> chats = (Map<Integer, ChatBase>) chatsField.get(null);

            synchronized (chats) {
                ChatBase chatBase = chats.get(chatID);
                if (chatBase != null) {
                    ChatMessage chatMessage = chatBase.unpackMessage(buffer);
                    if (chatMessage.getAuthor().trim().equalsIgnoreCase(connection.username)) return;

                    ModuleUtils.punishPlayer(getConfigName(), connection);
                }
            }
        } catch (Exception e) {
            System.out.println("[!] ChatImpersonator - Error processing player message:" + e.getMessage());
        }
    }
}