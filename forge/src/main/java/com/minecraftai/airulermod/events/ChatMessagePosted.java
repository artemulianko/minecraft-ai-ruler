package com.minecraftai.airulermod.events;

import net.minecraft.core.Vec3i;

public class ChatMessagePosted extends AbstractGameEvent {
    private final String message;

    public ChatMessagePosted(String playerId, String message, Vec3i pos) {
        super(playerId, pos);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
