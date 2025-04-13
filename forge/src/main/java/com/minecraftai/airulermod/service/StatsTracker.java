package com.minecraftai.airulermod.service;

import com.minecraftai.airulermod.stats.PlayerBuildingRate;
import com.minecraftai.airulermod.stats.PlayerMiningRate;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;

/**
 * Service responsible for tracking various player statistics 
 * and running timers to manage these statistics.
 */
@Singleton
public class StatsTracker {
    private static final Logger LOGGER = Logger.getLogger(StatsTracker.class.getName());

    private static final int MAX_EVENTS_PER_PLAYER = 200;
    private static final long MINUTE_IN_MS = 60 * 1000;
    private static final long CLEANUP_INTERVAL = 30000;

    private final Map<String, Deque<BlockEvent>> playerMiningEvents = new ConcurrentHashMap<>();
    private final Map<String, Deque<BlockEvent>> playerBuildingEvents = new ConcurrentHashMap<>();

    private Timer cleanupTimer;

    @Inject
    public StatsTracker() {}
    
    /**
     * Initialize timers for stats management
     */
    public void initializeTimers() {
        cleanupTimer = new Timer("StatsCleanupTimer", true);
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    cleanupOldEvents();
                } catch (Exception e) {
                    LOGGER.severe("Error in stats cleanup task: " + e.getMessage());
                }
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
    }

    /**
     * Track a block destroyed event for a player (mining)
     * @param playerId The player's UUID
     * @param timestamp The timestamp of the event
     */
    public void trackMiningEvent(String playerId, long timestamp) {
        // Get or create the player's event queue
        Deque<BlockEvent> playerEvents = playerMiningEvents.computeIfAbsent(
                playerId,
                k -> new ConcurrentLinkedDeque<>()
        );

        // Add the new mining event
        playerEvents.addLast(new BlockEvent(timestamp));

        // Remove oldest events if we exceed capacity
        while (playerEvents.size() > MAX_EVENTS_PER_PLAYER) {
            playerEvents.removeFirst();
        }
    }
    
    /**
     * Track a block placed event for a player (building)
     * @param playerId The player's UUID
     * @param timestamp The timestamp of the event
     */
    public void trackBuildingEvent(String playerId, long timestamp) {
        // Get or create the player's event queue
        Deque<BlockEvent> playerEvents = playerBuildingEvents.computeIfAbsent(
                playerId, 
                k -> new ConcurrentLinkedDeque<>()
        );

        // Add the new building event
        playerEvents.addLast(new BlockEvent(timestamp));

        // Remove oldest events if we exceed capacity
        while (playerEvents.size() > MAX_EVENTS_PER_PLAYER) {
            playerEvents.removeFirst();
        }
    }

    /**
     * Get the mining rate (blocks per minute) for a specific player
     * @param playerId The player's UUID
     * @return The number of blocks mined per minute
     */
    public int getMiningRate(String playerId) {
        return getBlockRateForPlayer(playerMiningEvents, playerId);
    }
    
    /**
     * Get the building rate (blocks per minute) for a specific player
     * @param playerId The player's UUID
     * @return The number of blocks placed per minute
     */
    public int getBuildingRate(String playerId) {
        return getBlockRateForPlayer(playerBuildingEvents, playerId);
    }
    
    /**
     * Helper method to calculate block rate for a player
     * @param eventsMap The map of events to use
     * @param playerId The player's UUID
     * @return The number of blocks per minute
     */
    private int getBlockRateForPlayer(Map<String, Deque<BlockEvent>> eventsMap, String playerId) {
        Deque<BlockEvent> playerEvents = eventsMap.get(playerId);
        if (playerEvents == null || playerEvents.isEmpty()) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        // Count blocks in the last minute
        return (int) playerEvents.stream()
                .filter(event -> currentTime - event.timestamp <= MINUTE_IN_MS)
                .count();
    }

    /**
     * Get the mining rates for all players
     * @return A map of player IDs to their mining rates
     */
    public Map<String, PlayerMiningRate> getAllMiningRates() {
        Map<String, PlayerMiningRate> rates = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        playerMiningEvents.forEach((playerId, events) -> {
            // Count blocks mined in the last minute
            int blocksPerMinute = (int) events.stream()
                    .filter(event -> currentTime - event.timestamp <= MINUTE_IN_MS)
                    .count();
            
            // Only include players with activity
            if (blocksPerMinute > 0) {
                rates.put(playerId, new PlayerMiningRate(playerId, blocksPerMinute));
            }
        });

        return rates;
    }
    
    /**
     * Get the building rates for all players
     * @return A map of player IDs to their building rates
     */
    public Map<String, PlayerBuildingRate> getAllBuildingRates() {
        Map<String, PlayerBuildingRate> rates = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        playerBuildingEvents.forEach((playerId, events) -> {
            // Count blocks placed in the last minute
            int blocksPerMinute = (int) events.stream()
                    .filter(event -> currentTime - event.timestamp <= MINUTE_IN_MS)
                    .count();
            
            // Only include players with activity
            if (blocksPerMinute > 0) {
                rates.put(playerId, new PlayerBuildingRate(playerId, blocksPerMinute));
            }
        });

        return rates;
    }

    /**
     * Clear old events that are no longer needed for calculation
     */
    public void cleanupOldEvents() {
        long cutoffTime = System.currentTimeMillis() - MINUTE_IN_MS;
        
        // Clean up mining events
        playerMiningEvents.forEach((playerId, events) -> {
            // Remove events older than 1 minute
            while (!events.isEmpty() && events.peekFirst().timestamp < cutoffTime) {
                events.removeFirst();
            }
        });
        
        // Clean up building events
        playerBuildingEvents.forEach((playerId, events) -> {
            // Remove events older than 1 minute
            while (!events.isEmpty() && events.peekFirst().timestamp < cutoffTime) {
                events.removeFirst();
            }
        });
    }
    
    /**
     * Shutdown timers when service is no longer needed
     */
    public void shutdown() {
        if (cleanupTimer != null) {
            cleanupTimer.cancel();
            cleanupTimer = null;
        }
    }

    /**
     * Represents a single block event with a timestamp
     */
    private record BlockEvent(long timestamp) {
    }
}