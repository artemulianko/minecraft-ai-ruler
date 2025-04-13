package com.minecraftai.airulermod.handler;

import com.minecraftai.airulermod.events.PlayerDamaged;
import com.minecraftai.airulermod.events.PlayerDied;
import com.minecraftai.airulermod.events.PlayerKilledEntity;
import com.minecraftai.airulermod.service.EventTracker;
import jakarta.inject.Inject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handler for player-related events such as taking damage, death, and kills
 */
public class PlayerEventsHandler {
    private final EventTracker eventTracker;
    
    @Inject
    public PlayerEventsHandler(EventTracker eventTracker) {
        this.eventTracker = eventTracker;
    }
    
    /**
     * Handles when a player takes damage
     */
    @SubscribeEvent
    public void onPlayerDamage(LivingDamageEvent event) {
        // Only track if the entity is a player and on the server side
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        String damageSourceName = event.getSource().getMsgId();
        
        // Track the damage event
        eventTracker.track(new PlayerDamaged(
            player.getStringUUID(),
            player.blockPosition(),
            event.getAmount(),
            damageSourceName
        ));
    }
    
    /**
     * Handles when a player dies
     */
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        // Only track if the entity is a player and on the server side
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        String deathCause = event.getSource().getMsgId();
        
        // Track the death event
        eventTracker.track(new PlayerDied(
            player.getStringUUID(),
            player.blockPosition(),
            deathCause
        ));
    }
    
    /**
     * Handles when a player kills another entity (player or mob)
     */
    @SubscribeEvent
    public void onEntityKilled(LivingDeathEvent event) {
        // Skip if on client side
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        // Check if the source is a player
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof Player player)) {
            return;
        }

        LivingEntity victim = event.getEntity();
        
        // Get the weapon used
        String weaponUsed = "hand";
        ItemStack itemInHand = player.getMainHandItem();
        if (!itemInHand.isEmpty()) {
            weaponUsed = itemInHand.getHoverName().getString();
        }
        
        // Track the kill event
        eventTracker.track(new PlayerKilledEntity(
            player.getStringUUID(),
            player.blockPosition(),
            victim.getType().toString(),
            victim.getStringUUID(),
            weaponUsed
        ));
    }
}