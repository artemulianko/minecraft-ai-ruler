package com.minecraftai.airulermod.stats;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a player's mining rate (blocks per minute).
 * This is a statistical metric rather than an event, used to track player activity.
 */
public class PlayerMiningRate {
    
    @SerializedName("pid")
    private final String playerId;
    
    @SerializedName("rate")
    private final int blocksPerMinute;
    
    @SerializedName("ts")
    private final long timestamp;
    
    public PlayerMiningRate(String playerId, int blocksPerMinute) {
        this.playerId = playerId;
        this.blocksPerMinute = blocksPerMinute;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public int getBlocksPerMinute() {
        return blocksPerMinute;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}