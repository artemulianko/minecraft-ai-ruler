package com.minecraftai.managermod.di;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.minecraft.server.MinecraftServer;

/**
 * A singleton class designed to hold and manage the reference to a
 * {@link MinecraftServer} instance. This class provides
 * functionality to set and retrieve the server instance. It ensures that
 * the server instance must be explicitly initialized before it can be retrieved.
 * <p>
 * This class is used as a dependency in other components to grant access to the
 * server instance within the application while maintaining safe initialization practices.
 */
@Singleton
public class ServerHolder {
    private MinecraftServer server;

    @Inject
    public ServerHolder() {}

    public MinecraftServer getServer() {
        if (server == null) {
            throw new IllegalStateException("Server is not initialized yet.");
        }

        return server;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }
}
