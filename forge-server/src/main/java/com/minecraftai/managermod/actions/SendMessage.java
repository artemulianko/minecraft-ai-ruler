package com.minecraftai.managermod.actions;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class SendMessage extends AbstractAction {
    private final String messageBody;

    public SendMessage(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * Executes the action to broadcast a predefined message to all players on the server.
     *
     * @param server the MinecraftServer instance on which the action is executed
     */
    @Override
    public void execute(MinecraftServer server) {
        // Create the message to broadcast
        Component message = Component.literal(messageBody); // Customize this message

        // Broadcast the message to all players
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
