package com.minecraftai.managermod.events;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ChatMessagePosted extends AbstractGameEvent {
    private final String playerName;
    private final String message;

    public ChatMessagePosted(String player, String message, ResourceKey<Level> dimension, Vec3i pos) {
        super(dimension, pos);
        this.playerName = player;
        this.message = message;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }
}
