package com.minecraftai.airulermod.service;

import com.google.gson.*;
import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.di.ServerHolder;
import com.minecraftai.airulermod.events.AbstractGameEvent;
import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.di.AIClientHolder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The EventsActionProcessor class handles the processing of game events, player statistics,
 * and responses from an AI system. It communicates with the AI to send data and retrieve actions,
 * which can then be executed in the server environment. It relies on several dependencies to
 * facilitate this functionality.
 * <p>
 * Dependencies include:
 * - {@link ServerHolder}: Provides access to the Minecraft server and player data.
 * - {@link AIClient}: Interfaces with the AI system for sending events or statistics and
 *   receiving responses.
 * - {@link Gson}: Used for JSON serialization of events, statistics, and other data.
 * - {@link ActionsParser}: Parses responses received from the AI system into actionable entities.
 * <p>
 * This processor supports three main workflows:
 * - Send game events to the AI without processing the actions immediately. Actions are instead
 *   handled separately via a ping mechanism.
 * - Send player statistics to the AI, which are treated independently from the game events.
 * - Use a ping mechanism to request actions from the AI based on events or statistics that were
 *   previously sent.
 */
@Singleton
public class EventsActionProcessor {
    private static final Logger LOGGER = Logger.getLogger(EventsActionProcessor.class.getName());
    
    private final ServerHolder serverHolder;
    private final AIClient aiClient;
    private final Gson serializer;
    private final ActionsParser actionsParser;
    
    @Inject
    public EventsActionProcessor(
            AIClientHolder aiClientHolder,
            ServerHolder serverHolder,
            Gson serializer,
            ActionsParser actionsParser
    ) {
        this.aiClient = aiClientHolder.getAiClient();
        this.serverHolder = serverHolder;
        this.serializer = serializer;
        this.actionsParser = actionsParser;
    }

    /**
     * Processes a list of game events by sending them to the AI.
     * With the new ping mechanism, we don't process actions here anymore.
     */
    public void sendEvents(List<AbstractGameEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        
        Map<String, Position> playersPositions = this.serverHolder
                .getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .collect(Collectors.toMap(Entity::getStringUUID, it -> it.getOnPos().getCenter()));

        try {
            final var serverBatchMessage = serializer.toJson(Map.of(
                    "events", events,
                    "playerPositions", playersPositions
            ));
            LOGGER.fine("Sending events to AI: " + serverBatchMessage);
            
            final var aiResponse = aiClient.chat(serverBatchMessage);
            LOGGER.info("AI response to events received, length: " + 
                   (aiResponse != null ? aiResponse.message().length() : 0) + " characters");
            
            if (aiResponse == null) {
                LOGGER.warning("Empty response received from AI for events");
            }
            
            // We don't process actions from events directly anymore
            // The ping mechanism will request actions separately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process AI response for events", e);
        }
    }
    
    /**
     * Process player statistics separately from game events.
     * This allows for different handling of stats vs. events.
     *
     * @param stats Collection of player statistics (mining and building rates)
     */
    public void sendStats(Collection<?> stats) {
        if (stats.isEmpty()) {
            return;
        }
        
        Map<String, Position> playersPositions = this.serverHolder
                .getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .collect(Collectors.toMap(Entity::getStringUUID, it -> it.getOnPos().getCenter()));

        try {
            final var statsBatchMessage = serializer.toJson(Map.of(
                    "stats", stats,
                    "playerPositions", playersPositions
            ));
            LOGGER.fine("Sending stats to AI: " + statsBatchMessage);
            
            final var aiResponse = aiClient.chat(statsBatchMessage);
            LOGGER.info("AI response to stats received, length: " + 
                   (aiResponse != null ? aiResponse.message().length() : 0) + " characters");
            
            if (aiResponse == null) {
                LOGGER.warning("Empty response received from AI for stats");
            }
            
            // We don't process actions from stats or events directly anymore
            // The ping mechanism will request actions separately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process AI response for stats", e);
        }
    }
    
    /**
     * Send a ping message to the AI to request actions based on previously sent events/stats.
     * This is the only method that actually returns actions to execute.
     *
     * @return List of actions to perform based on the AI's response
     */
    public @Nullable List<AbstractAction> ping() {
        try {
            LOGGER.fine("Sending ping to AI to request actions");
            
            // Use the correct ping format as specified in the prompt
            final var aiResponse = aiClient.chat("{\"ping\":true}");
            LOGGER.info("AI response to ping received, length: " + 
                   (aiResponse != null ? aiResponse.message().length() : 0) + " characters");
            
            if (aiResponse == null) {
                LOGGER.warning("Empty response received from AI for ping");
                return null;
            }
            
            // Process the response to get actions
            JsonObject responseJson = JsonParser.parseString(aiResponse.message()).getAsJsonObject();
            
            // Check if actions field exists and is an array
            if (!responseJson.has("actions") || !responseJson.get("actions").isJsonArray()) {
                LOGGER.warning("No actions field in AI response");
                return null;
            }
            
            JsonArray actions = responseJson.get("actions").getAsJsonArray();
            
            // If array is empty, return empty list (no actions to take)
            if (actions.isEmpty()) {
                LOGGER.fine("AI returned empty actions array - no actions to take");
                return List.of();
            }

            List<AbstractAction> actionList = new LinkedList<>();
            for (JsonElement action : actions) {
                JsonObject actionJson = action.getAsJsonObject();
                final var actionEntity = actionsParser.parse(actionJson);

                if (actionEntity != null) {
                    actionList.add(actionEntity);
                }
            }

            LOGGER.info("Processed " + actionList.size() + " actions from ping response");
            return actionList;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process AI response for ping", e);
            return null;
        }
    }
}