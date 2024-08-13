package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;

/**
 * Extra Info
 */
public class ExtraInfoModule extends Module {
    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public ExtraInfoModule() {
        super(PacketTypes.PacketType.ExtraInfo, "extraInfo", "Extra Info", "Blocking the use of commands for unauthorized access");
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
