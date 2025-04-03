package com.minecraftai.managermod.handler;

import com.minecraftai.managermod.actions.AbstractAction;
import com.minecraftai.managermod.events.AbstractGameEvent;
import com.minecraftai.managermod.events.ChatMessagePosted;
import com.minecraftai.managermod.service.ActionsProcessor;
import com.minecraftai.managermod.service.EventTracker;
import com.minecraftai.managermod.service.EventsActionResponder;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerEventsHandler {
    private static final ExecutorService releaseEventsThread = Executors.newSingleThreadExecutor();

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ActionsProcessor.init(event.getServer());

        releaseEventsThread.submit(() ->
                new Timer(true).scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        List<AbstractGameEvent> events = EventTracker.releaseEvents();
                        List<AbstractAction> actions = EventsActionResponder.respond(events);
                        ActionsProcessor.scheduleActions(actions);
                    }
                }, 0, 5000)
        );
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Process all pending actions on the server thread
            ActionsProcessor.processActions();
        }
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        var player = event.getPlayer();

        EventTracker.track(new ChatMessagePosted(
                player.getStringUUID(),
                event.getRawText(),
                player.level().dimension(),
                player.getOnPos()
            )
        );
    }
}
