package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferReader;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.iso.areas.SafeHouse;
import zombie.network.PacketTypes;
import zombie.network.ServerMap;

import java.nio.ByteBuffer;

/**
 * SafeHouse Items
 */
public class SafehouseItemsModule extends Module {
    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public SafehouseItemsModule() {
        super(PacketTypes.PacketType.RemoveInventoryItemFromContainer, "safeHouseItems", "SafeHouse Items", "Blocking theft of things from private");
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
        ByteBufferReader reader = new ByteBufferReader(buffer);
        short actionType = reader.getShort();
        int gridX = reader.getInt();
        int gridY = reader.getInt();
        int gridZ = reader.getInt();

        IsoGridSquare gridSquare = IsoWorld.instance.CurrentCell.getGridSquare(gridX, gridY, gridZ);
        if (gridSquare == null) {
            gridSquare = ServerMap.instance.getGridSquare(gridX, gridY, gridZ);
        }
        
        SafeHouse safeHouse = SafeHouse.getSafeHouse(gridSquare);

        if (safeHouse == null) return;

        if (safeHouse != SafeHouse.hasSafehouse(player)) {
            ModuleUtils.punishPlayer(getConfigName(), connection);
        }
    }
}