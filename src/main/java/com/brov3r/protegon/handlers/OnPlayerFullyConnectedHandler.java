package com.brov3r.protegon.handlers;

import com.avrix.events.OnPlayerFullyConnectedEvent;
import com.brov3r.protegon.modules.VpnAC;
import com.brov3r.protegon.utils.InventoryTools;
import zombie.core.raknet.UdpConnection;

import java.nio.ByteBuffer;

/**
 * Handles the event when a player is fully connected to the server.
 * Extends {@link OnPlayerFullyConnectedEvent} to override handling method.
 */
public class OnPlayerFullyConnectedHandler extends OnPlayerFullyConnectedEvent {
    /**
     * Handles the event when a player is fully connected by processing the necessary actions.
     *
     * @param byteBuffer    the ByteBuffer containing event data
     * @param udpConnection the UDP connection associated with the player
     * @param username      player username
     */
    @Override
    public void handleEvent(ByteBuffer byteBuffer, UdpConnection udpConnection, String username) {
        VpnAC.handlePacket(udpConnection);
        InventoryTools.requestPlayerInventory(udpConnection);
    }
}