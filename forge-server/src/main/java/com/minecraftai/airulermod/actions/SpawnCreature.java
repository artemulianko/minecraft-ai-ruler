package com.minecraftai.airulermod.actions;

import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnCreature extends AbstractAction {
    public static final String ACTION_TYPE = "SpawnCreature";

    enum CreatureType {
        COW,
        CHICKEN,
        CREEPER,
        SPIDER;
    }

    private static final Map<String, EntityType<?>> entityMap = Map.of(
            CreatureType.COW.name(), EntityType.COW,
            CreatureType.CHICKEN.name(), EntityType.CHICKEN,

            CreatureType.CREEPER.name(), EntityType.CREEPER,
            CreatureType.SPIDER.name(), EntityType.SPIDER
    );

    private final Vec3i pos;
    private final String creatureType;

    public SpawnCreature(String creatureType, Vec3i pos) {
        this.creatureType = creatureType;
        this.pos = pos;
    }

    @Override
    public void execute(MinecraftServer server) {
        var level = getLevel(server);
        if (level == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "No level found.");
            return;
        }

        EntityType<?> entity = entityMap.get(creatureType);
        if (entity == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unknown entity type " + creatureType);
            return;
        }


        final var entityInstance = entity.create(level, EntitySpawnReason.COMMAND);

        if (entityInstance != null) {
            entityInstance.setPos(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            level.addFreshEntity(entityInstance);
        }
    }
}
