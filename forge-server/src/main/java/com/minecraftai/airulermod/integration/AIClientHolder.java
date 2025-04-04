package com.minecraftai.airulermod.integration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AIClientHolder {
    private final AIClient aiClient;

    @Inject
    public AIClientHolder(@AIImplementation(AIType.OPENAI) AIClient aiClient) {
        this.aiClient = aiClient;
    }

    public AIClient getAiClient() {
        return aiClient;
    }
}
