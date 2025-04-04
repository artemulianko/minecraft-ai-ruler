package com.minecraftai.airulermod.service;

import com.google.gson.*;
import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.actions.SendMessage;
import com.minecraftai.airulermod.actions.SpawnBlock;
import com.minecraftai.airulermod.actions.SpawnCreature;
import com.minecraftai.airulermod.di.ServerHolder;
import com.minecraftai.airulermod.events.AbstractGameEvent;
import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.di.AIClientHolder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class EventsActionResponder {
    private final ServerHolder serverHolder;
    private final AIClient aiClient;
    private final Gson serializer;

    @Inject
    public EventsActionResponder(
            AIClientHolder aiClientHolder,
            ServerHolder serverHolder,
            Gson serializer
    ) {
        this.aiClient = aiClientHolder.getAiClient();
        this.serverHolder = serverHolder;
        this.serializer = serializer;
    }

    /**
     * Processes a list of game events and generates a corresponding list of actions.
     * If the event list is empty, no actions are returned.
     */
    public @Nullable List<AbstractAction> respond(List<AbstractGameEvent> events) {
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
            System.out.println(serverBatchMessage);

            final var aiResponse = aiClient.chat(serverBatchMessage);
            System.out.println("AI RESPONSE: " + aiResponse);

            if (aiResponse == null) {
                System.err.println("Empty response from AI");
                return null;
            }

            JsonArray actions = JsonParser.parseString(aiResponse.message())
                    .getAsJsonObject()
                    .get("actions")
                    .getAsJsonArray();

            List<AbstractAction> actionList = new LinkedList<>();
            for (JsonElement action : actions) {
                JsonObject actionJson = action.getAsJsonObject();
                String actionType = actionJson.get("type").getAsString();

                try {
                    AbstractAction actionEntity = switch (actionType) {
                        case "SPAWN_CREATURE" -> new SpawnCreature(
                                actionJson.get("creatureType").getAsString(),
                                serializer.fromJson(actionJson.get("pos"), Vec3i.class)
                        );
                        case "PLACE_BLOCK" -> new SpawnBlock(
                                actionJson.get("blockType").getAsString(),
                                serializer.fromJson(actionJson.get("pos"), Vec3i.class)
                        );
                        case "SEND_MESSAGE" -> new SendMessage(actionJson.get("messageBody").getAsString());
                        default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
                    };

                    actionList.add(actionEntity);
                } catch (IllegalArgumentException e) {
                    System.err.println("Failed to deserialize action: " + actionJson);
                }
            }

            return actionList;
        } catch (Exception e) {
            System.err.println("Failed to serialize events to JSON" + e);
            return null;
        }
    }
}
