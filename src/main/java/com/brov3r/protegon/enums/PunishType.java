package com.brov3r.protegon.enums;

/**
 * Enumerates types of punishment that can be applied to a player.
 */
public enum PunishType {
    /**
     * No punishment is applied.
     */
    NOTHING,

    /**
     * Logging the offense without any active punishment.
     */
    LOGGING,

    /**
     * Kicking the player from the server as a punishment.
     */
    KICK,

    /**
     * Banning the player from the server as a punishment.
     */
    BAN
}