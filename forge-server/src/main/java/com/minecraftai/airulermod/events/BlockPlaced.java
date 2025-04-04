package com.minecraftai.airulermod.events;

import net.minecraft.core.Vec3i;

public class BlockPlaced extends AbstractGameEvent {
    public BlockPlaced(String playerId, Vec3i pos) {
        super(playerId, pos);
    }
}
