package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;

/**
 * Explosive
 */
public class ExplosiveModule extends Module {
    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public ExplosiveModule() {
        super(PacketTypes.PacketType.AddExplosiveTrap, "explosive", "Explosive", "Bomb spawn blocking");
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
        ModuleUtils.punishPlayer(getConfigName(), connection);
    }
}
