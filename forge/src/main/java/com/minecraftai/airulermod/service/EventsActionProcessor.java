package com.minecraftai.airulermod.service;

import com.google.gson.*;
import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.di.ServerHolder;
import com.minecraftai.airulermod.events.AbstractGameEvent;
import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.integration.AIClientManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
            AIClientManager aiClientManager,
            ServerHolder serverHolder,
            Gson serializer,
            ActionsParser actionsParser
    ) {
        this.aiClient = aiClientManager.getAiClient();
        this.serverHolder = serverHolder;
        this.serializer = serializer;
        this.actionsParser = actionsParser;
    }

    public @Nullable List<AbstractAction> process(
            Collection<AbstractGameEvent> events,
            Map<String, Map<String, ?>> stats
    ) {
        final var playersPositions = collectPositions();

        final var serverBatchMessage = serializer.toJson(Map.of(
                "events", events,
                "stats", stats,
                "playerPositions", playersPositions
        ));
        LOGGER.fine("serverBatchMessage: " + serverBatchMessage);

        final var aiResponse = aiClient.chat(serverBatchMessage);
        LOGGER.info("AI response to events received, length: " +
                (aiResponse != null ? aiResponse.message().length() : 0) + " characters");

        if (aiResponse == null) {
            LOGGER.warning("Empty response received from AI for events");
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
    }

    private Map<String, Position> collectPositions() {
        return this.serverHolder
                .getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .collect(Collectors.toMap(Entity::getStringUUID, it -> it.getOnPos().getCenter()));
    }
}