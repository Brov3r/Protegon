package com.brov3r.protegon.handlers;

import com.avrix.events.OnPlayerKickEvent;
import com.brov3r.protegon.utils.DiscordWebhook;
import zombie.core.raknet.UdpConnection;

/**
 * Handling player kicks
 */
public class OnPlayerKickHandler extends OnPlayerKickEvent {
    /**
     * Called Event Handling Method
     *
     * @param udpConnection Connection of the player who was kicked.
     * @param adminName     Nickname of the administrator who banned the player
     * @param reason        Reason for blocking the player.
     */
    @Override
    public void handleEvent(UdpConnection udpConnection, String adminName, String reason) {
        DiscordWebhook.sendMessage(udpConnection, adminName, reason);
    }
}