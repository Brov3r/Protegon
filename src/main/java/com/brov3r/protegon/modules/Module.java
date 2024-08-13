package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;

/**
 * The {@code Module} class represents a base module in the Protegon system that handles specific types of packets.
 * Each module is characterized by a packet type, a name, and a description. This class is abstract,
 * meaning that specific modules must extend it and implement the {@link #handlePacket(ByteBuffer buffer, IsoPlayer player, UdpConnection connection)} method.
 */
public abstract class Module {
    /**
     * The type of packet that this module handles.
     */
    private final PacketTypes.PacketType packetType;

    /**
     * The name of the module.
     */
    private final String name;

    /**
     * The name of the module in config.
     */
    private final String configName;

    /**
     * A brief description of what this module does.
     */
    private final String description;

    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     *
     * @param type        The packet type that this module handles, defined in {@link PacketTypes.PacketType}.
     * @param configName  The name of the module in config.
     * @param name        The name of the module.
     * @param description A brief description of the module's functionality.
     */
    public Module(PacketTypes.PacketType type, String configName, String name, String description) {
        this.description = description;
        this.packetType = type;
        this.name = name;
        this.configName = configName;
    }

    /**
     * Determines whether a packet should be ignored based on the module's configuration and the player's rights.
     *
     * @param player The {@link IsoPlayer} object representing the player associated with the packet.
     * @return {@code true} if the packet should be ignored; {@code false} otherwise.
     */
    public boolean isIgnorePacket(IsoPlayer player) {
        return !Main.getConfig().getBoolean(configName + ".isEnable") || ModuleUtils.isPlayerHasRights(player);
    }

    /**
     * Returns the name of this module in config.
     *
     * @return The name of the module in config.
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * Returns the packet type that this module handles.
     *
     * @return The packet type associated with this module.
     */
    public PacketTypes.PacketType getPacketType() {
        return packetType;
    }

    /**
     * Returns the description of this module.
     *
     * @return The description of the module's functionality.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of this module.
     *
     * @return The name of the module.
     */
    public String getName() {
        return name;
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
    public abstract void handlePacket(ByteBuffer buffer, IsoPlayer player, UdpConnection connection);
}