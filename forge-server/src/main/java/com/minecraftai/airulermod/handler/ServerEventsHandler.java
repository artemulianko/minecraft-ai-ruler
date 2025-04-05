package com.minecraftai.airulermod.handler;

import com.google.gson.JsonObject;
import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.constants.Prompts;
import com.minecraftai.airulermod.di.ServerHolder;
import com.minecraftai.airulermod.events.*;
import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.di.AIClientHolder;
import com.minecraftai.airulermod.service.ActionsProcessor;
import com.minecraftai.airulermod.service.EventTracker;
import com.minecraftai.airulermod.service.EventsActionProcessor;
import com.minecraftai.airulermod.service.StatsService;
import com.minecraftai.airulermod.stats.PlayerBuildingRate;
import com.minecraftai.airulermod.stats.PlayerMiningRate;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerEventsHandler {
    private static final Logger LOGGER = Logger.getLogger(ServerEventsHandler.class.getName());
    private static final ExecutorService releaseEventsThread = Executors.newSingleThreadExecutor();
    
    // Polling intervals in milliseconds
    private static final long ACTIVE_POLLING_INTERVAL = 15000; // 15 seconds
    private static final long IDLE_POLLING_INTERVAL = 60000;   // 60 seconds
    private static final long STATS_POLLING_INTERVAL = 30000;  // 30 seconds
    private static final long PING_INTERVAL = 5000;            // 5 seconds
    
    // Number of consecutive empty event batches before switching to idle polling
    private static final int EMPTY_BATCH_THRESHOLD = 3;
    
    private final ActionsProcessor actionsProcessor;
    private final EventsActionProcessor eventsActionResponder;
    private final EventTracker eventTracker;
    private final StatsService statsService;
    private final ServerHolder serverHolder;
    private final AIClient aiClient;
    
    // Track consecutive empty event batches
    private int consecutiveEmptyBatches = 0;
    
    // Timers for different types of polling
    private Timer eventsTimer;
    private Timer statsTimer;
    private Timer pingTimer;
    
    // Current events polling rate
    private long currentEventsInterval = ACTIVE_POLLING_INTERVAL;
    
    // Track when last events and stats were sent
    private long lastEventsSent = 0;
    private long lastStatsSent = 0;

    @Inject
    public ServerEventsHandler(
            ServerHolder serverHolder,
            ActionsProcessor actionsProcessor,
            EventsActionProcessor eventsActionResponder,
            EventTracker eventTracker,
            StatsService statsService,
            AIClientHolder aiClientHolder
    ) {
        this.serverHolder = serverHolder;
        this.actionsProcessor = actionsProcessor;
        this.eventsActionResponder = eventsActionResponder;
        this.eventTracker = eventTracker;
        this.statsService = statsService;
        this.aiClient = aiClientHolder.getAiClient();
    }
    
    /**
     * Determines if an event is significant for polling rate purposes
     * @param event The event to check
     * @return true if the event is significant, false otherwise
     */
    private boolean isSignificantEvent(AbstractGameEvent event) {
        // Chat messages and player deaths are always significant
        return event instanceof ChatMessagePosted
                || event instanceof PlayerDied
                || event instanceof PlayerKilledEntity
                || event instanceof PlayerDamaged;
    }
    
    /**
     * Adjusts the events polling rate based on activity level
     * @param events List of events in the current batch
     */
    private void adjustEventsPollingRate(List<AbstractGameEvent> events) {
        boolean hasSignificantEvents = false;
        
        // Check if there are any significant events
        if (!events.isEmpty()) {
            for (AbstractGameEvent event : events) {
                if (isSignificantEvent(event)) {
                    hasSignificantEvents = true;
                    break;
                }
            }
        }
        
        // If no events or no significant events, increment counter
        if (events.isEmpty() || !hasSignificantEvents) {
            consecutiveEmptyBatches++;
        } else {
            // Reset counter if there are significant events
            consecutiveEmptyBatches = 0;
        }
        
        // Adjust polling rate based on counter
        long newPollingInterval = currentEventsInterval;
        
        if (consecutiveEmptyBatches >= EMPTY_BATCH_THRESHOLD) {
            // Switch to idle polling
            newPollingInterval = IDLE_POLLING_INTERVAL;
        } else if (hasSignificantEvents || events.size() > 3) {
            // Switch to active polling if significant events or many events
            newPollingInterval = ACTIVE_POLLING_INTERVAL;
        }
        
        // If polling rate changed, restart timer
        if (newPollingInterval != currentEventsInterval) {
            currentEventsInterval = newPollingInterval;
            LOGGER.info("Adjusting events polling interval to " + (currentEventsInterval / 1000) + " seconds");
            restartEventsTimer();
        }
    }
    
    /**
     * Initialize all timers
     */
    private void initializeAllTimers() {
        // Initialize events timer
        eventsTimer = new Timer("EventsTimer", true);
        eventsTimer.scheduleAtFixedRate(createEventsTask(), 0, currentEventsInterval);
        
        // Initialize stats timer with fixed 30 second interval
        statsTimer = new Timer("StatsTimer", true);
        statsTimer.scheduleAtFixedRate(createStatsTask(), STATS_POLLING_INTERVAL, STATS_POLLING_INTERVAL);
        
        // Initialize ping timer with fixed 5 second interval
        pingTimer = new Timer("PingTimer", true);
        pingTimer.scheduleAtFixedRate(createPingTask(), PING_INTERVAL, PING_INTERVAL);
    }
    
    /**
     * Stop all timers
     */
    private void stopAllTimers() {
        if (eventsTimer != null) {
            eventsTimer.cancel();
            eventsTimer = null;
        }
        
        if (statsTimer != null) {
            statsTimer.cancel();
            statsTimer = null;
        }
        
        if (pingTimer != null) {
            pingTimer.cancel();
            pingTimer = null;
        }
    }
    
    /**
     * Restarts the events timer with the current interval
     */
    private void restartEventsTimer() {
        if (eventsTimer != null) {
            eventsTimer.cancel();
        }
        
        eventsTimer = new Timer("EventsTimer", true);
        eventsTimer.scheduleAtFixedRate(createEventsTask(), 0, currentEventsInterval);
    }
    
    /**
     * Creates a task for collecting and processing game events
     */
    private TimerTask createEventsTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    // Get regular game events
                    List<AbstractGameEvent> events = eventTracker.releaseEvents();
                    
                    // Adjust polling rate based on events
                    // adjustEventsPollingRate(events);
                    
                    // Process events if there are any
                    if (!events.isEmpty()) {
                        // Send events to AI
                        eventsActionResponder.sendEvents(events);
                        lastEventsSent = System.currentTimeMillis();
                        LOGGER.fine("Sent " + events.size() + " events to AI");
                    }
                } catch (Exception e) {
                    LOGGER.severe("Error in events task: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }
    
    /**
     * Creates a task for collecting and processing player statistics
     */
    private TimerTask createStatsTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    // Get player mining statistics
                    Map<String, PlayerMiningRate> miningRates = statsService.getAllMiningRates();
                    
                    // Get player building statistics
                    Map<String, PlayerBuildingRate> buildingRates = statsService.getAllBuildingRates();
                    
                    // Combine stats into a collection
                    List<Object> allStats = new ArrayList<>();
                    allStats.addAll(miningRates.values());
                    allStats.addAll(buildingRates.values());
                    
                    // Process stats if there are any
                    if (!allStats.isEmpty()) {
                        // Send stats to AI
                        eventsActionResponder.sendStats(allStats);
                        lastStatsSent = System.currentTimeMillis();
                        LOGGER.fine("Sent stats for " + allStats.size() + " player activities to AI");
                    }
                } catch (Exception e) {
                    LOGGER.severe("Error in stats task: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }
    
    /**
     * Creates a task for sending ping messages to get actions
     */
    private TimerTask createPingTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    // We always send ping messages at a regular interval
                    // The AI will decide whether to send actions based on previously sent events/stats

                    // Send ping and get actions
                    List<AbstractAction> actions = eventsActionResponder.ping();
                    
                    // Process actions if there are any
                    if (actions != null && !actions.isEmpty()) {
                        actionsProcessor.scheduleActions(actions);
                        LOGGER.info("Processed " + actions.size() + " actions from ping");
                    }
                } catch (Exception e) {
                    LOGGER.severe("Error in ping task: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        serverHolder.setServer(event.getServer());
        aiClient.setupInstructions(Prompts.getInstructions());
        aiClient.sendInstructions();

        // Initialize timestamps
        lastEventsSent = System.currentTimeMillis();
        lastStatsSent = System.currentTimeMillis();

        // Start all timers
        releaseEventsThread.submit(this::initializeAllTimers);
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
            // Cancel the event
            event.setCanceled(true);
            
            // Notify the player they are muted
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You cannot chat while muted."));
            
            // Log the blocked message
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
        
        // Chat messages are significant - reset to active polling immediately
        if (currentEventsInterval != ACTIVE_POLLING_INTERVAL) {
            consecutiveEmptyBatches = 0;
            currentEventsInterval = ACTIVE_POLLING_INTERVAL;
            LOGGER.info("Chat message detected, switching to active polling rate");
            restartEventsTimer();
        }
        
        // Mark that we've just sent events to update the ping timer logic
        lastEventsSent = System.currentTimeMillis();
    }
}
