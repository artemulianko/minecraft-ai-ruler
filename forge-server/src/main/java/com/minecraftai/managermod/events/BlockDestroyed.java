package com.minecraftai.managermod.events;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BlockDestroyed extends AbstractGameEvent {
    public BlockDestroyed(ResourceKey<Level> dimension, Vec3i pos) {
        super(dimension, pos);
    }
}
