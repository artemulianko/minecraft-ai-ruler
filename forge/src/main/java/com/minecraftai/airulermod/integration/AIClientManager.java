package com.minecraftai.airulermod.integration;

import com.minecraftai.airulermod.config.EnvConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class AIClientManager {
    private final Map<String, AIClient> aiClientMap;
    private final EnvConfig envConfig;

    @Inject
    public AIClientManager(Map<String, AIClient> aiClientMap, EnvConfig envConfig) {
        this.aiClientMap = aiClientMap;
        this.envConfig = envConfig;
    }

    /**
     * Retrieves an AIClient instance based on the configuration in the environment variables.
     * If no specific AI client type is defined, defaults to the "MOCK" client implementation.
     *
     * @return The AIClient instance determined by the environment configuration.
     *         Defaults to a mock implementation if no configuration is provided.
     */
    public AIClient getAiClient() {
        final var aiClientImplementation = envConfig.getOrDefault("AI_CLIENT", "MOCK");
        return getAiClientInternal(aiClientImplementation);
    }

    /**
     * Retrieves an instance of an AIClient based on the provided client type.
     *
     * @param clientType The type of AI client to be retrieved (e.g., "OPENAI", "MOCK").
     *                   This must correspond to a key present in the internal client map.
     * @return The corresponding AIClient instance for the specified client type.
     * @throws IllegalArgumentException If the provided client type is not recognized or not present in the map.
     */
    private AIClient getAiClientInternal(String clientType) {
        if (aiClientMap.containsKey(clientType)) return aiClientMap.get(clientType);

        throw new IllegalArgumentException("Unknown client type: " + clientType);
    }
}
