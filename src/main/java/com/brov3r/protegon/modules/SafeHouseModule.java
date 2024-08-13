package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.iso.areas.SafeHouse;
import zombie.network.PacketTypes;
import zombie.network.packets.SyncSafehousePacket;

import java.nio.ByteBuffer;

/**
 * Safehouse
 */
public class SafeHouseModule extends Module {
    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public SafeHouseModule() {
        super(PacketTypes.PacketType.SyncSafehouse, "safeHouse", "SafeHouse", "Blocking the ability to delete other people's safehouse");
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
        
        SyncSafehousePacket packet = new SyncSafehousePacket();
        packet.parse(buffer, connection);
        if (packet.validate(connection)) {
            if (packet.safehouse != null) {
                SafeHouse safeHouse = packet.safehouse;

                System.out.printf("[#] Safehouse - Player '%s' sent a packet to safehouse '%s' (x: %s, y: %s, owner: %s, remove: %s)%n",
                        connection.username, safeHouse.getTitle(), safeHouse.getX(), safeHouse.getY(), safeHouse.getOwner(), packet.remove);
            }

            if (!packet.remove) return;

            if (packet.ownerUsername.equalsIgnoreCase(player.getUsername())) return;

            ModuleUtils.punishPlayer(getConfigName(), connection);
        }
    }
}