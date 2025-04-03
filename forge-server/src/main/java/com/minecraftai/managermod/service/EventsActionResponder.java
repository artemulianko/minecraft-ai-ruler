package com.minecraftai.managermod.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftai.managermod.actions.AbstractAction;
import com.minecraftai.managermod.actions.SpawnCreature;
import com.minecraftai.managermod.config.JsonConfig;
import com.minecraftai.managermod.di.ServerHolder;
import com.minecraftai.managermod.events.AbstractGameEvent;
import com.minecraftai.managermod.integration.OpenAIClient;
import jakarta.inject.Inject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventsActionResponder {
    private final static ObjectMapper objectMapper = JsonConfig.createObjectMapper();
    private final ServerHolder serverHolder;
    private final OpenAIClient openAIClient;

    @Inject
    public EventsActionResponder(ServerHolder serverHolder, OpenAIClient openAIClient) {
        this.serverHolder = serverHolder;
        this.openAIClient = openAIClient;
    }

    /**
     * Processes a list of game events and generates a corresponding list of actions.
     * If the event list is empty, no actions are returned.
     */
    public List<AbstractAction> respond(List<AbstractGameEvent> events) {
        Map<String, BlockPos> playersPositions = this.serverHolder
                .getServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .collect(Collectors.toMap(Entity::getStringUUID, Entity::getOnPos));

        try {
            String serverBatchMessage = objectMapper.writeValueAsString(Map.of(
                    "events", events,
                    "playersPositions", playersPositions
            ));
            System.out.println(serverBatchMessage);

            final String aiResponse = openAIClient.sendMessage(serverBatchMessage);
            System.out.println("AI RESPONSE: " + aiResponse);

//            System.out.println(aiResponse);
            // Log or use the eventsJson as needed
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize events to JSON", e);
        }

        if (events.isEmpty()) {
            return List.of();
        }

        return List.of();
    }
}
