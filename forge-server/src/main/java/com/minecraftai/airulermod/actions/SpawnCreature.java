package com.minecraftai.airulermod.actions;

import com.minecraftai.airulermod.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnCreature extends AbstractAction {
    public static final String ACTION_TYPE = "SpawnCreature";

    private static final Map<String, EntityType<?>> ENTITY_MAP = Map.ofEntries(
        Map.entry("COW", EntityType.COW),
        Map.entry("CHICKEN", EntityType.CHICKEN),
        Map.entry("CREEPER", EntityType.CREEPER),
        Map.entry("SPIDER", EntityType.SPIDER),
        Map.entry("PIG", EntityType.PIG),
        Map.entry("SHEEP", EntityType.SHEEP),
        Map.entry("ZOMBIE", EntityType.ZOMBIE),
        Map.entry("SKELETON", EntityType.SKELETON),
        Map.entry("WOLF", EntityType.WOLF),
        Map.entry("SLIME", EntityType.SLIME),
        Map.entry("VILLAGER", EntityType.VILLAGER),
        Map.entry("HORSE", EntityType.HORSE),
        Map.entry("RABBIT", EntityType.RABBIT),
        Map.entry("SQUID", EntityType.SQUID),
        Map.entry("ENDERMAN", EntityType.ENDERMAN)
    );

    private final Vec3i pos;
    private final String creatureType;

    public SpawnCreature(String creatureType, Vec3i pos) {
        this.creatureType = creatureType;
        this.pos = pos;
    }

    /**
     * Returns a list of all available creature types as strings
     * @return List of creature type names
     */
    public static List<String> getAvailableCreatureTypes() {
        return new ArrayList<>(ENTITY_MAP.keySet());
    }

    @Override
    public void execute(MinecraftServer server) {
        var level = getLevel(server);
        if (level == null) {
            getLogger().severe("No level found.");
            return;
        }

        EntityType<?> entity = ENTITY_MAP.get(creatureType);
        if (entity == null) {
            getLogger().severe("Unknown entity type " + creatureType);
            return;
        }

        final var entityInstance = entity.create(level, EntitySpawnReason.COMMAND);
        if (entityInstance == null) {
            getLogger().severe("Cannot create entity of type " + creatureType);
            return;
        }

        final BlockPos safePos = PositionUtils.findNearestEmptyPosition(level, pos);
        if (safePos == null) {
            getLogger().severe("No safe position found for " + creatureType + " at " + pos);
            return;
        }

        entityInstance.setPos(safePos.getX(), safePos.getY(), safePos.getZ());
        level.addFreshEntity(entityInstance);
    }
}
