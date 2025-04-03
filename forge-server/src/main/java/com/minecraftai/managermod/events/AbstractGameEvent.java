package com.minecraftai.managermod.events;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class AbstractGameEvent {
    private final ResourceKey<Level> dimension;
    private final Vec3i pos;
    private final long timestamp;


    public AbstractGameEvent(ResourceKey<Level> dimension, Vec3i pos) {
        timestamp = System.currentTimeMillis();
        this.dimension = dimension;
        this.pos = pos;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public Vec3i getPos() {
        return pos;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
