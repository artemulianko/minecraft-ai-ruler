package com.minecraftai.airulermod.actions;

import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnCreature extends AbstractAction {
    private static final Map<String, EntityType<?>> entityMap = Map.of(
            "COW", EntityType.COW,
            "CHICKEN", EntityType.CHICKEN,

            "CREEPER", EntityType.CREEPER,
            "SPIDER", EntityType.SPIDER
    );

    private final Vec3i position;
    private final String entityType;

    public SpawnCreature(String entityType, Vec3i position) {
        this.entityType = entityType;
        this.position = position;
    }

    @Override
    public void execute(MinecraftServer server) {
        var level = getLevel(server);
        if (level == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "No level found.");
            return;
        }

        EntityType<?> entity = entityMap.get(entityType);
        if (entity == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unknown entity type " + entityType);
            return;
        }


        final var entityInstance = entity.create(level, EntitySpawnReason.COMMAND);

        if (entityInstance != null) {
            entityInstance.setPos(this.position.getX(), this.position.getY(), this.position.getZ());
            level.addFreshEntity(entityInstance);
        }
    }
}
