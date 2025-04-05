package com.minecraftai.airulermod.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Vec3i;

/**
 * Event generated when a player takes damage from any source
 */
public class PlayerDamaged extends AbstractGameEvent {
    
    @SerializedName("dmg")
    private final float damageAmount;
    
    @SerializedName("src")
    private final String damageSource;
    
    public PlayerDamaged(String playerId, Vec3i pos, float damageAmount, String damageSource) {
        super(playerId, pos);
        this.damageAmount = damageAmount;
        this.damageSource = damageSource;
    }
    
    public float getDamageAmount() {
        return damageAmount;
    }
    
    public String getDamageSource() {
        return damageSource;
    }
}