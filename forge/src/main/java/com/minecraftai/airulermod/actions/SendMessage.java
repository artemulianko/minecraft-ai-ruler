package com.minecraftai.airulermod.actions;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class SendMessage extends AbstractAction {
    public static final String ACTION_TYPE = "SendMessage";

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
        Component message = Component.literal(messageBody);

        // Broadcast the message to all players
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
