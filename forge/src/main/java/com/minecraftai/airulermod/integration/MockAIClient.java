package com.minecraftai.airulermod.integration;

import com.google.gson.Gson;
import com.minecraftai.airulermod.actions.SendMessage;
import com.minecraftai.airulermod.actions.SpawnBlock;
import jakarta.inject.Singleton;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

@Singleton
public class MockAIClient implements AIClient {
    private final Gson serializer = new Gson();

    @Inject
    public MockAIClient() {}

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
    public void setInstructions(String instructions) {
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
                                "type", SendMessage.class.getSimpleName(),
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
                                "type", SpawnBlock.class.getSimpleName(),
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
                                "type", SpawnBlock.class.getSimpleName(),
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
                                "type", SpawnBlock.class.getSimpleName(),
                                "creatureType", "COW",
                                "pos", Map.of("x", 10, "y", 10, "z", 10)
                        )
                )
        );

        return new ChatResponse(serializer.toJson(responseBody));
    }
}
