package com.minecraftai.managermod.actions;

import net.minecraft.server.MinecraftServer;

public abstract class AbstractAction {
    public String name;

    public AbstractAction(String name) {
        this.name = name;
    }

    /**
     * Executes the action using the specified MinecraftServer instance.
     * The implementation of this method defines the specific behavior of the action.
     *
     * @param server the MinecraftServer instance on which the action is executed
     */
    public abstract void execute(MinecraftServer server);
}
