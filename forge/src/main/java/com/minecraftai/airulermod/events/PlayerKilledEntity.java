package com.minecraftai.airulermod.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Vec3i;

/**
 * Event generated when a player kills an entity (mob, animal, or another player)
 */
public class PlayerKilledEntity extends AbstractGameEvent {
    
    @SerializedName("target")
    private final String targetEntityType;
    
    @SerializedName("targetId")
    private final String targetEntityId;
    
    @SerializedName("weapon")
    private final String weaponUsed;
    
    public PlayerKilledEntity(String playerId, Vec3i pos, String targetEntityType, String targetEntityId, String weaponUsed) {
        super(playerId, pos);
        this.targetEntityType = targetEntityType;
        this.targetEntityId = targetEntityId;
        this.weaponUsed = weaponUsed;
    }
    
    public String getTargetEntityType() {
        return targetEntityType;
    }
    
    public String getTargetEntityId() {
        return targetEntityId;
    }
    
    public String getWeaponUsed() {
        return weaponUsed;
    }
}