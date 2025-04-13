package com.minecraftai.airulermod.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Vec3i;

/**
 * Event generated when a player dies
 */
public class PlayerDied extends AbstractGameEvent {
    
    @SerializedName("cause")
    private final String deathCause;
    
    public PlayerDied(String playerId, Vec3i pos, String deathCause) {
        super(playerId, pos);
        this.deathCause = deathCause;
    }
    
    public String getDeathCause() {
        return deathCause;
    }
}