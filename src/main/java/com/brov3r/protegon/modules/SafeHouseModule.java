package com.brov3r.protegon.modules;

import com.brov3r.protegon.utils.ModuleUtils;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.iso.areas.SafeHouse;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;

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

        SafeHouse safeHouse = SafeHouse.hasSafehouse(player);

        int x = buffer.getInt();
        int y = buffer.getInt();
        short width = buffer.getShort();
        short height = buffer.getShort();

        SafeHouse squareHouse = SafeHouse.getSafeHouse(x, y, width, height);

        String ownerUsername = GameWindow.ReadString(buffer);
        short membersSize = buffer.getShort();
        ArrayList<String> members = new ArrayList<>();
        ArrayList<String> membersRespawn = new ArrayList<>();

        for (int i = 0; i < membersSize; ++i) {
            members.add(GameWindow.ReadString(buffer));
        }

        short membersRespawnSize = buffer.getShort();

        for (int i = 0; i < membersRespawnSize; ++i) {
            membersRespawn.add(GameWindow.ReadString(buffer));
        }

        boolean remove = buffer.get() == 1;
        String title = GameWindow.ReadString(buffer);

        System.out.printf("[#] Safehouse - Player '%s' sent a packet to safehouse '%s' (x: %s, y: %s, size: %s, owner: %s, remove: %s, members: %s, members respawn: %s)%n",
                connection.username, title, x, y, width * height, ownerUsername, remove, members, membersRespawn);

        if (safeHouse != squareHouse || (remove && !ownerUsername.equalsIgnoreCase(player.getUsername()))) {
            ModuleUtils.punishPlayer(getConfigName(), connection);
        }
    }
}