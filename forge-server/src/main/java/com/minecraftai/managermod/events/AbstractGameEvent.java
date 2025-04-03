package com.minecraftai.managermod.events;

import net.minecraft.core.Vec3i;

public abstract class AbstractGameEvent {
    /**
     * Represents the unique identifier of a player associated with the game event.
     * This ID is used to track which player triggered or is involved in a specific event.
     */
    private final String playerId;

    /**
     * Represents the position related to a specific game event in a three-dimensional
     * coordinate system. Typically used to identify the location where the event occurred,
     * such as block placement, destruction, or other positional events in the game world.
     */
    private final Vec3i pos;

    /**
     * The timestamp indicating the exact moment when the game event was created.
     * This value is automatically initialized to the current system time
     * in milliseconds when the event instance is constructed.
     */
    private final long timestamp;


    public AbstractGameEvent(String playerId, Vec3i pos) {
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
        this.pos = pos;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Vec3i getPos() {
        return pos;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventName() {
        return this.getClass().getSimpleName();
    }
}
