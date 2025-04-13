package com.minecraftai.airulermod.integration;

public interface AIClient {
    record ChatResponse(String message) {}
    record ChatMessage(String role, String input) {}

    void setInstructions(String instructions);
    ChatResponse chat(String userMessage);
    void sendInstructions();
}
