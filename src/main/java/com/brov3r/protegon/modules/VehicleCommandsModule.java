package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * VehicleCommands
 */
public class VehicleCommandsModule extends Module {
    /**
     * List of vehicle cheats that trigger anti-cheat actions
     */
    private final List<String> vehicleCheats = new ArrayList<>() {{
        add("getKey");
        add("cheatHotwire");
        add("repairPart");
        add("repair");
        add("setRust");
        add("setPartCondition");
    }};

    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public VehicleCommandsModule() {
        super(PacketTypes.PacketType.ClientCommand, "vehicleCommands", "Vehicle Commands", "Blocking cheat commands on vehicle");
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

        if (!command.equalsIgnoreCase("vehicle")) return;

        // Check if the method matches any of the vehicle cheats
        for (String cheatCmd : vehicleCheats) {
            if (method.equals(cheatCmd)) {
                // Punish the player for using the detected cheat command
                ModuleUtils.punishPlayer(getConfigName(), connection);
                return;
            }
        }
    }
}