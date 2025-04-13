package com.minecraftai.airulermod.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Tracks the approximate token count for conversations with the AI.
 * Used to determine when to refresh instructions to prevent context overflow.
 */
@Singleton
public class TokenCounter {
    // Token threshold at which we should resend instructions
    private static final long TOKEN_REFRESH_THRESHOLD = 80000L;
    
    // Average characters per token (a rough approximation)
    private static final float CHARS_PER_TOKEN = 4.0f;
    
    // Total count of characters sent and received
    private long characterCount = 0;
    
    @Inject
    public TokenCounter() {}
    
    /**
     * Adds characters to the count and returns whether the token threshold has been reached
     * @param messageChars Number of characters in the message
     * @return True if instructions should be resent, false otherwise
     */
    public synchronized boolean addMessage(int messageChars) {
        characterCount += messageChars;
        
        // Check if we've reached the threshold and need to refresh
        if (getEstimatedTokenCount() >= TOKEN_REFRESH_THRESHOLD) {
            resetCounter();
            return true;
        }
        
        return false;
    }
    
    /**
     * Estimates the token count based on character count
     * @return Estimated token count
     */
    public long getEstimatedTokenCount() {
        return Math.round(characterCount / CHARS_PER_TOKEN);
    }
    
    /**
     * Resets the counter after instructions have been refreshed
     */
    public synchronized void resetCounter() {
        characterCount = 0;
    }
}