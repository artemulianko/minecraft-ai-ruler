package com.minecraftai.managermod.handler;

import com.minecraftai.managermod.events.BlockDestroyed;
import com.minecraftai.managermod.events.BlockPlaced;
import com.minecraftai.managermod.service.EventTracker;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockEventsHandler {
    @SubscribeEvent
    public void onBlockAdded(BlockEvent.EntityPlaceEvent event) {
        // Ignore client events
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() != null) {
            EventTracker.track(new BlockPlaced(event.getEntity().level().dimension(), event.getPos()));
        } else {
            // Handle cases where the block was not placed by a known entity
            System.out.printf("Added BlockEvent {%s} by {unknown entity}%n", event.getPos().toString());
        }
    }

    @SubscribeEvent
    public void onBlockDestroyed(BlockEvent.BreakEvent event) {
        // Ignore client events
        if (event.getLevel().isClientSide()) return;

        EventTracker.track(new BlockDestroyed(event.getPlayer().level().dimension(), event.getPos()));
    }
}
