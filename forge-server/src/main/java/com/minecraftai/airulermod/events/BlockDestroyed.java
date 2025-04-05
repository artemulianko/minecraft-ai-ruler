package com.minecraftai.airulermod.events;

import net.minecraft.core.Vec3i;

public class BlockDestroyed extends AbstractGameEvent {
    public BlockDestroyed(String playerId, Vec3i pos) {
        super(playerId, pos);
    }
}
