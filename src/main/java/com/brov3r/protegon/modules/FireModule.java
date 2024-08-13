package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Fire
 */
public class FireModule extends Module {
    /**
     * Cheats commands
     */
    private final List<String> fireCheats = new ArrayList<>() {{
        add("addFireOnSquare");
        add("addSmokeOnSquare");
        add("addExplosionOnSquare");
    }};

    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public FireModule() {
        super(PacketTypes.PacketType.ClientCommand, "fire", "Fire", "Blocking cheat commands with a fire");
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
        byte index = buffer.get();
        String command = zombie.GameWindow.ReadString(buffer);
        String method = zombie.GameWindow.ReadString(buffer);
        boolean isTable = buffer.get() == 1;

        if (command.equalsIgnoreCase("object")) {

            for (String cheatCmd : fireCheats) {
                if (method.equalsIgnoreCase(cheatCmd)) {
                    ModuleUtils.punishPlayer(getConfigName(), connection);
                    return;
                }
            }
        }
    }
}