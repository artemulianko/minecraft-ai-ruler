package com.minecraftai.airulermod.actions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Action to mute or unmute a player in chat.
 */
public class MutePlayer extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(MutePlayer.class.getName());
    public static final String ACTION_TYPE = "MutePlayer";
    
    // Static set to track muted players across the server
    private static final Set<String> MUTED_PLAYERS = new HashSet<>();
    
    private final String playerId;
    private final boolean mute;  // true to mute, false to unmute
    private final String reason;
    
    /**
     * Creates a new MutePlayer action.
     * 
     * @param playerId The UUID of the player to mute/unmute
     * @param mute True to mute the player, false to unmute
     * @param reason The reason for the mute/unmute action
     */
    public MutePlayer(String playerId, boolean mute, String reason) {
        this.playerId = playerId;
        this.mute = mute;
        this.reason = reason != null ? reason : (mute ? "Muted by AI Moderator" : "Unmuted by AI Moderator");
    }
    
    /**
     * Checks if a player is currently muted
     * @param playerId The UUID of the player to check
     * @return true if the player is muted, false otherwise
     */
    public static boolean isPlayerMuted(String playerId) {
        return MUTED_PLAYERS.contains(playerId);
    }
    
    @Override
    public void execute(MinecraftServer server) {
        try {
            // Find the player by UUID
            ServerPlayer player = null;
            try {
                UUID playerUUID = UUID.fromString(playerId);
                player = server.getPlayerList().getPlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid player UUID format: " + playerId, e);
                return;
            }
            
            // Update the mute status
            if (mute) {
                MUTED_PLAYERS.add(playerId);
                LOGGER.info("Muted player with ID " + playerId + " for reason: " + reason);
            } else {
                MUTED_PLAYERS.remove(playerId);
                LOGGER.info("Unmuted player with ID " + playerId);
            }
            
            // Notify the player if they're online
            if (player != null) {
                String messageText = mute 
                    ? "You have been muted: " + reason
                    : "You have been unmuted.";
                
                player.sendSystemMessage(Component.literal(messageText));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to " + (mute ? "mute" : "unmute") + " player " + playerId, e);
        }
    }
}