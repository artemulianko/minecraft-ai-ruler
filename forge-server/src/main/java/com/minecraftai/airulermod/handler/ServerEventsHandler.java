package com.minecraftai.airulermod.handler;

import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.constants.Prompts;
import com.minecraftai.airulermod.di.ServerHolder;
import com.minecraftai.airulermod.events.AbstractGameEvent;
import com.minecraftai.airulermod.events.ChatMessagePosted;
import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.integration.AIClientHolder;
import com.minecraftai.airulermod.service.ActionsProcessor;
import com.minecraftai.airulermod.service.EventTracker;
import com.minecraftai.airulermod.service.EventsActionResponder;
import jakarta.inject.Inject;
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

    private final ActionsProcessor actionsProcessor;
    private final EventsActionResponder eventsActionResponder;
    private final EventTracker eventTracker;
    private final ServerHolder serverHolder;
    private final AIClient aiClient;

    @Inject
    public ServerEventsHandler(
            ServerHolder serverHolder,
            ActionsProcessor actionsProcessor,
            EventsActionResponder eventsActionResponder,
            EventTracker eventTracker,
            AIClientHolder aiClientHolder
    ) {
        this.serverHolder = serverHolder;
        this.actionsProcessor = actionsProcessor;
        this.eventsActionResponder = eventsActionResponder;
        this.eventTracker = eventTracker;
        this.aiClient = aiClientHolder.getAiClient();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        serverHolder.setServer(event.getServer());
        aiClient.setupInstructions(Prompts.getInstructions());
        aiClient.sendInstructions();

        releaseEventsThread.submit(() ->
                new Timer(true).scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        List<AbstractGameEvent> events = eventTracker.releaseEvents();
                        List<AbstractAction> actions = eventsActionResponder.respond(events);

                        if (actions != null) actionsProcessor.scheduleActions(actions);
                    }
                }, 0, 15000)
        );
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Process all pending actions on the server thread
            actionsProcessor.processActions();
        }
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        var player = event.getPlayer();

        eventTracker.track(new ChatMessagePosted(
                        player.getStringUUID(),
                        event.getRawText(),
                        player.getOnPos()
                )
        );
    }
}
