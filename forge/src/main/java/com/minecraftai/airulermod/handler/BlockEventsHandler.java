package com.minecraftai.airulermod.handler;

import com.minecraftai.airulermod.service.StatsTracker;
import jakarta.inject.Inject;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.logging.Logger;

public class BlockEventsHandler {
    private static final Logger LOGGER = Logger.getLogger(BlockEventsHandler.class.getName());
    
    private final StatsTracker statsService;

    @Inject
    public BlockEventsHandler(StatsTracker statsService) {
        this.statsService = statsService;
    }

    @SubscribeEvent
    public void onBlockAdded(BlockEvent.EntityPlaceEvent event) {
        // Ignore client events
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() != null) {
            // Track block placement for building stats
            String playerId = event.getEntity().getStringUUID();
            long timestamp = System.currentTimeMillis();
            statsService.trackBuildingEvent(playerId, timestamp);
        } else {
            // Handle cases where the block was not placed by a known entity
            LOGGER.info(String.format("Added BlockEvent {%s} by {unknown entity}", event.getPos().toString()));
        }
    }

    @SubscribeEvent
    public void onBlockDestroyed(BlockEvent.BreakEvent event) {
        // Ignore client events
        if (event.getLevel().isClientSide()) return;

        // Track mining stats directly without creating an event object
        String playerId = event.getPlayer().getStringUUID();
        long timestamp = System.currentTimeMillis();
        statsService.trackMiningEvent(playerId, timestamp);
    }
}
