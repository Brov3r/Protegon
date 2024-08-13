package com.brov3r.protegon.handlers;

import com.avrix.events.OnPlayerBanEvent;
import com.brov3r.protegon.utils.DiscordWebhook;
import zombie.core.raknet.UdpConnection;

/**
 * Handling player bans
 */
public class OnPlayerBanHandler extends OnPlayerBanEvent {
    /**
     * Called Event Handling Method
     *
     * @param udpConnection Connection of the player who was banned.
     * @param adminName     Nickname of the administrator who banned the player
     * @param reason        Reason for blocking the player.
     */
    @Override
    public void handleEvent(UdpConnection udpConnection, String adminName, String reason) {
        DiscordWebhook.sendMessage(udpConnection, adminName, reason);
    }
}