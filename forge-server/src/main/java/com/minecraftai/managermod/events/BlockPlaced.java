package com.minecraftai.managermod.events;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BlockPlaced extends AbstractGameEvent {
    public BlockPlaced(String playerId, Vec3i pos) {
        super(playerId, pos);
    }
}
