package com.minecraftai.airulermod.events;
import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Vec3i;

public abstract class AbstractGameEvent {
    /**
     * Represents the unique identifier of a player associated with the game event.
     * This ID is used to track which player triggered or is involved in a specific event.
     */
    @SerializedName("pid")
    private final String playerId;

    /**
     * Represents the position related to a specific game event in a three-dimensional
     * coordinate system. Typically used to identify the location where the event occurred,
     * such as block placement, destruction, or other positional events in the game world.
     */
    @SerializedName("pos")
    private final Vec3i pos;

    /**
     * The timestamp indicating the exact moment when the game event was created.
     * This value is automatically initialized to the current system time
     * in milliseconds when the event instance is constructed.
     */
    @SerializedName("ts")
    private final long timestamp;

    /**
     * Represents the name of the game event. This value is dynamically derived from the class name
     * of the event instance. It is used to identify the specific type of event, such as "BlockPlaced",
     * "BlockDestroyed", or "ChatMessagePosted".
     */
    @SerializedName("event")
    private final String eventName;

    public AbstractGameEvent(String playerId, Vec3i pos) {
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
        this.pos = pos;
        eventName = this.getClass().getSimpleName();
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
        return eventName;
    }
}
