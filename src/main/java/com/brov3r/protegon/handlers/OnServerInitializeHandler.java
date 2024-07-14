package com.brov3r.protegon.handlers;

import com.avrix.events.OnServerInitializeEvent;
import com.brov3r.protegon.modules.InventoryAC;

/**
 * Handles the event when the server initializes.
 * Extends {@link OnServerInitializeEvent} to override handling method.
 */
public class OnServerInitializeHandler extends OnServerInitializeEvent {
    /**
     * Handles the event when the server initializes by initiating inventory updates.
     */
    @Override
    public void handleEvent() {
        InventoryAC.initInventoryUpdate();
    }
}