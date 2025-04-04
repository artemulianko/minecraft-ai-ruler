package com.minecraftai.airulermod.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MockAIClient implements AIClient {
    @Override
    public ChatResponse chat(String userMessage) {
        JsonObject mockAction = new JsonObject();
        mockAction.addProperty("type", "SEND_MESSAGE");
        mockAction.addProperty("messageBody", "Response from mock AI.");

        JsonArray actions = new JsonArray();
        actions.add(mockAction);

        JsonObject response = new JsonObject();
        response.add("actions", actions);

        return new ChatResponse(response.toString());
    }

    @Override
    public void setupInstructions(String instructions) {
        // Do nothing
    }


    @Override
    public void sendInstructions() {
        // Do nothing
    }
}
