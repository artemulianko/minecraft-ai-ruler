package com.minecraftai.airulermod.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MockAIClient implements AIClient {
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
        JsonObject mockAction = new JsonObject();
        mockAction.addProperty("type", "SEND_MESSAGE");
        mockAction.addProperty("messageBody", "Response from mock AI.");

        JsonArray actions = new JsonArray();
        actions.add(mockAction);

        JsonObject response = new JsonObject();
        response.add("actions", actions);

        return new ChatResponse(response.toString());
    }

    private ChatResponse createMudBlock() {
        JsonObject mockAction = new JsonObject();
        mockAction.addProperty("type", "PLACE_BLOCK");
        mockAction.addProperty("blockType", "MUD");
        JsonObject pos = new JsonObject();
        pos.addProperty("x", 10);
        pos.addProperty("y", 10);
        pos.addProperty("z", 10);

        mockAction.add("pos", pos);

        JsonArray actions = new JsonArray();
        actions.add(mockAction);

        JsonObject response = new JsonObject();
        response.add("actions", actions);

        return new ChatResponse(response.toString());
    }

    private ChatResponse createTNTBlock() {
        JsonObject mockAction = new JsonObject();
        mockAction.addProperty("type", "PLACE_BLOCK");
        mockAction.addProperty("blockType", "TNT");

        JsonObject pos = new JsonObject();
        pos.addProperty("x", 10);
        pos.addProperty("y", 10);
        pos.addProperty("z", 10);

        mockAction.add("pos", pos);

        JsonArray actions = new JsonArray();
        actions.add(mockAction);

        JsonObject response = new JsonObject();
        response.add("actions", actions);

        return new ChatResponse(response.toString());
    }

    private ChatResponse spawnCow() {
        JsonObject mockAction = new JsonObject();
        mockAction.addProperty("type", "SPAWN_CREATURE");
        mockAction.addProperty("creatureType", "COW");

        JsonObject pos = new JsonObject();
        pos.addProperty("x", 10);
        pos.addProperty("y", 10);
        pos.addProperty("z", 10);

        mockAction.add("pos", pos);

        JsonArray actions = new JsonArray();
        actions.add(mockAction);

        JsonObject response = new JsonObject();
        response.add("actions", actions);

        return new ChatResponse(response.toString());
    }
}
