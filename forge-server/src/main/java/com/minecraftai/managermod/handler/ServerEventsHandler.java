package com.minecraftai.managermod.handler;

import com.minecraftai.managermod.actions.AbstractAction;
import com.minecraftai.managermod.constants.Prompts;
import com.minecraftai.managermod.di.ServerHolder;
import com.minecraftai.managermod.events.AbstractGameEvent;
import com.minecraftai.managermod.events.ChatMessagePosted;
import com.minecraftai.managermod.integration.OpenAIClient;
import com.minecraftai.managermod.service.ActionsProcessor;
import com.minecraftai.managermod.service.EventTracker;
import com.minecraftai.managermod.service.EventsActionResponder;
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
    private final OpenAIClient openAIClient;

    @Inject
    public ServerEventsHandler(
            ServerHolder serverHolder,
            ActionsProcessor actionsProcessor,
            EventsActionResponder eventsActionResponder,
            EventTracker eventTracker,
            OpenAIClient openAIClient
    ) {
        this.serverHolder = serverHolder;
        this.actionsProcessor = actionsProcessor;
        this.eventsActionResponder = eventsActionResponder;
        this.eventTracker = eventTracker;
        this.openAIClient = openAIClient;
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        openAIClient.setupInstructions(List.of(
                Prompts.DESCRIBE_AI_ROLE,
                Prompts.DESCRIBE_ACTIONS
        ));

        serverHolder.setServer(event.getServer());

        releaseEventsThread.submit(() ->
                new Timer(true).scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        List<AbstractGameEvent> events = eventTracker.releaseEvents();
                        List<AbstractAction> actions = eventsActionResponder.respond(events);
                        actionsProcessor.scheduleActions(actions);
                    }
                }, 0, 5000)
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
                        player.level().dimension(),
                        player.getOnPos()
                )
        );
    }
}
