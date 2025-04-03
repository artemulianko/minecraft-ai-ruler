package com.minecraftai.managermod.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.inject.Singleton;

import javax.inject.Inject;
import java.nio.file.Paths;

@Singleton
public class EnvConfig {
    private final Dotenv dotenv;

    @Inject
    public EnvConfig() {
        // Load .env file
        this.dotenv = loadDotenv();
    }

    private Dotenv loadDotenv() {
        // Try multiple locations
        String[] possiblePaths = {
                ".",                          // Project root (development)
                "src/main/resources",         // Standard resources directory
                System.getProperty("user.dir"), // Absolute path
                "/config"                     // Common deployment location
        };

        Exception lastException = null;

        for (String path : possiblePaths) {
            try {
                return Dotenv.configure()
                        .directory(path)
                        .ignoreIfMissing()
                        .load();
            } catch (Exception e) {
                lastException = e;
                System.out.println("Failed to load .env from: " +
                        Paths.get(path).toAbsolutePath());
            }
        }

        throw new RuntimeException("Could not load .env from any location", lastException);
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

