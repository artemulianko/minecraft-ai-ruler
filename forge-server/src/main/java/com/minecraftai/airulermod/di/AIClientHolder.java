package com.minecraftai.airulermod.di;

import com.minecraftai.airulermod.integration.AIClient;
import com.minecraftai.airulermod.integration.AIImplementation;
import com.minecraftai.airulermod.integration.AIType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AIClientHolder {
    private final AIClient aiClient;

    @Inject
    public AIClientHolder(@AIImplementation(AIType.MOCK) AIClient aiClient) {
        this.aiClient = aiClient;
    }

    public AIClient getAiClient() {
        return aiClient;
    }
}
