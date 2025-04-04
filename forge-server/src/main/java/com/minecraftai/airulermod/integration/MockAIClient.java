package com.minecraftai.airulermod.integration;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class MockAIClient implements AIClient {
    private final Gson serializer = new Gson();

    @Override
    public ChatResponse chat(String userMessage) {
        return switch (userMessage) {
            case "MUD" -> createMudBlock();
            case "TNT" -> createTNTBlock();
            case "COW" -> spawnCow();
            default ->  sendMockMessage();
        };
    }

    @Override
    public void setupInstructions(String instructions) {
        // Do nothing
    }


    @Override
    public void sendInstructions() {
        // Do nothing
    }

    private ChatResponse sendMockMessage() {
        final var responseBody = Map.of(
                "actions", List.of(
                        Map.of(
                                "type", "SEND_MESSAGE",
                                "messageBody", "Response from mock AI."
                        )
                )
        );

        return new ChatResponse(serializer.toJson(responseBody));
    }

    private ChatResponse createMudBlock() {
        final var responseBody = Map.of(
                "actions", List.of(
                        Map.of(
                                "type", "PLACE_BLOCK",
                                "blockType", "MUD",
                                "pos", Map.of("x", 10, "y", 10, "z", 10)
                        )
                )
        );

        return new ChatResponse(serializer.toJson(responseBody));
    }

    private ChatResponse createTNTBlock() {
        final var responseBody = Map.of(
                "actions", List.of(
                        Map.of(
                                "type", "PLACE_BLOCK",
                                "blockType", "TNT",
                                "pos", Map.of("x", 10, "y", 10, "z", 10)
                        )
                )
        );

        return new ChatResponse(serializer.toJson(responseBody));
    }

    private ChatResponse spawnCow() {
        final var responseBody = Map.of(
                "actions", List.of(
                        Map.of(
                                "type", "SPAWN_CREATURE",
                                "creatureType", "COW",
                                "pos", Map.of("x", 10, "y", 10, "z", 10)
                        )
                )
        );

        return new ChatResponse(serializer.toJson(responseBody));
    }
}
