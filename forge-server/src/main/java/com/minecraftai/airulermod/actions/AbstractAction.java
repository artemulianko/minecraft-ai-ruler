package com.minecraftai.airulermod.actions;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public abstract class AbstractAction {
    /**
     * Executes the action using the specified MinecraftServer instance.
     * The implementation of this method defines the specific behavior of the action.
     *
     * @param server the MinecraftServer instance on which the action is executed
     */
    public abstract void execute(MinecraftServer server);

    /**
     * Retrieves the dimension to be used for an action. First attempts to get the dimension
     * of the world of the first online player. If no players are online, defaults to the overworld dimension.
     *
     * @param server the MinecraftServer instance used to retrieve the dimension
     * @return the dimension to be used, either from an online player or the overworld as a fallback
     */
    protected ResourceKey<Level> getDimension(MinecraftServer server) {
        var playerDimension = server.getPlayerList().getPlayers().stream()
                .findFirst()
                .map(player -> player.level().dimension());

        return playerDimension.orElse(Level.OVERWORLD);
    }

    /**
     * Retrieves the Level associated with a specific dimension of the given MinecraftServer.
     * The dimension is determined based on the first online player's world; if no players are online,
     * the default overworld dimension is used.
     *
     * @param server the MinecraftServer instance used to retrieve the Level
     * @return the Level corresponding to the determined dimension, or null if no such Level exists
     */
    protected @Nullable Level getLevel(MinecraftServer server) {
        final var dimension = getDimension(server);
        return server.getLevel(dimension);
    }

    /**
     * Retrieves the simple name of the class implementing the method.
     *
     * @return the name of the class as a String
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Retrieves a Logger instance associated with the class implementing this method.
     *
     * @return a Logger instance configured for the current class
     */
    protected final Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }
}
