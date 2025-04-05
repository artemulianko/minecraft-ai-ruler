package com.minecraftai.airulermod.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.inject.Singleton;

import javax.inject.Inject;

@Singleton
public class EnvConfig {
    private final Dotenv dotenv;

    @Inject
    public EnvConfig() {
        this.dotenv = Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();
    }

    /**
     * Fetch an environment variable by key.
     *
     * @param key The key to fetch from .env file.
     * @return The value of the environment variable, or null if not found.
     */
    public String get(String key) {
        return dotenv.get(key);
    }

    /**
     * Fetch an environment variable by key, with a default fallback.
     *
     * @param key          The key to fetch from .env file.
     * @param defaultValue The default value to use if key is not found.
     * @return The value of the variable, or default if not found.
     */
    public String getOrDefault(String key, String defaultValue) {
        return dotenv.get(key, defaultValue);
    }
}

