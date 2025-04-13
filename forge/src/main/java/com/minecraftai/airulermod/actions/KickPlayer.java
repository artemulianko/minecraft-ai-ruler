package com.minecraftai.airulermod.actions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Action to kick a player from the server with a specified reason.
 */
public class KickPlayer extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(KickPlayer.class.getName());
    public static final String ACTION_TYPE = "KickPlayer";
    
    private final String playerId;
    private final String reason;
    
    /**
     * Creates a new KickPlayer action.
     * 
     * @param playerId The UUID of the player to kick
     * @param reason The reason for kicking the player
     */
    public KickPlayer(String playerId, String reason) {
        this.playerId = playerId;
        this.reason = reason != null ? reason : "Kicked by AI Moderator";
    }
    
    @Override
    public void execute(MinecraftServer server) {
        try {
            ServerPlayer player;

            try {
                UUID playerUUID = UUID.fromString(playerId);
                player = server.getPlayerList().getPlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid player UUID format: " + playerId, e);
                return;
            }
            
            if (player == null) {
                LOGGER.warning("Cannot kick player: Player with ID " + playerId + " not found or offline");
                return;
            }

            Component kickMessage = Component.literal(reason);
            
            // Kick the player
            LOGGER.info("Kicking player " + player.getName().getString() + " (" + playerId + ") for reason: " + reason);
            player.connection.disconnect(kickMessage);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to kick player " + playerId, e);
        }
    }
}