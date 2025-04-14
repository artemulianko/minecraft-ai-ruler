package com.minecraftai.airulermod.handler;

import com.minecraftai.airulermod.constants.Prompts;
import com.minecraftai.airulermod.di.ServerHolder;
import com.minecraftai.airulermod.events.*;
import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.integration.AIClientManager;
import com.minecraftai.airulermod.service.*;
import jakarta.inject.Inject;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.logging.Logger;

/**
 * Handles server-level events and manages interactions between various components.
 *
 * This class listens to and processes important server events such as server start,
 * server tick, chat messages, and server stop. It acts as a mediator between server
 * operations and different subsystems including actions processing, event tracking,
 * stats tracking, and AI communication. Reactive changes and interactions with the server
 * occur through subscribed event handlers.
 *
 * Responsibilities:
 * - Initiates and stops components on server lifecycle events.
 * - Schedules and processes AI communications.
 * - Ensures proper handling of chat messages, including muted player checks.
 * - Processes queued actions on every server tick.
 */
public class ServerEventsHandler {
    private static final Logger LOGGER = Logger.getLogger(ServerEventsHandler.class.getName());
    
    private final ActionsProcessor actionsProcessor;

    private final EventTracker eventTracker;
    private final StatsTracker statsService;
    private final ServerHolder serverHolder;
    private final AIClient aiClient;
    private final AICommunicationScheduler aiCommunicationScheduler;

    @Inject
    public ServerEventsHandler(
            ServerHolder serverHolder,
            ActionsProcessor actionsProcessor,
            EventTracker eventTracker,
            StatsTracker statsService,
            AIClientManager aiClientManager,
            AICommunicationScheduler aiCommunicationScheduler
    ) {
        this.serverHolder = serverHolder;
        this.actionsProcessor = actionsProcessor;
        this.eventTracker = eventTracker;
        this.statsService = statsService;
        this.aiClient = aiClientManager.getAiClient();
        this.aiCommunicationScheduler = aiCommunicationScheduler;
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Start tracking players stats
        statsService.initializeTimers();
        serverHolder.setServer(event.getServer());

        // Initialize communication with AI
        aiClient.setInstructions(Prompts.getInstructions());
        aiClient.sendInstructions();

        // Schedule communication with AI
        aiCommunicationScheduler.start();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        aiCommunicationScheduler.stop();
        statsService.shutdown();
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
        String playerId = player.getStringUUID();
        
        // Check if the player is muted
        if (com.minecraftai.airulermod.actions.MutePlayer.isPlayerMuted(playerId)) {
            event.setCanceled(true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You cannot chat while muted."));
            LOGGER.info("Blocked chat message from muted player: " + player.getName().getString());
            
            return;
        }

        // Track the chat message event
        eventTracker.track(new ChatMessagePosted(
                        playerId,
                        event.getRawText(),
                        player.getOnPos()
                )
        );
    }
}
