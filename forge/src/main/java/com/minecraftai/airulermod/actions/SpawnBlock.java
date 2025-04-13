package com.minecraftai.airulermod.actions;

import com.minecraftai.airulermod.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnBlock extends AbstractAction {
    public static final String ACTION_TYPE = "SpawnBlock";

    private final String blockType;
    private final Vec3i pos;

    /**
     * Means:
     * 1. - Only make the change visible to clients without causing block updates
     * 2. - Only notify neighbors without updating clients (rarely used alone)
     */
    private static int SPAWN_BLOCK_MODE = 3;

    // Map of all available block types that can be spawned
    private static final Map<String, Block> BLOCK_MAP = Map.ofEntries(
        // Original blocks
        Map.entry("MUD", Blocks.MUD),
        Map.entry("WATER", Blocks.WATER),
        Map.entry("LAVA", Blocks.LAVA),
        Map.entry("ANVIL", Blocks.ANVIL),
        Map.entry("TNT", Blocks.TNT),
        
        // New blocks
        Map.entry("STONE", Blocks.STONE),
        Map.entry("DIRT", Blocks.DIRT),
        Map.entry("GRASS_BLOCK", Blocks.GRASS_BLOCK),
        Map.entry("SAND", Blocks.SAND),
        Map.entry("GRAVEL", Blocks.GRAVEL),
        Map.entry("BEDROCK", Blocks.BEDROCK),
        Map.entry("GOLD_BLOCK", Blocks.GOLD_BLOCK),
        Map.entry("DIAMOND_BLOCK", Blocks.DIAMOND_BLOCK),
        Map.entry("EMERALD_BLOCK", Blocks.EMERALD_BLOCK),
        Map.entry("IRON_BLOCK", Blocks.IRON_BLOCK)
    );

    public SpawnBlock(String blockType, Vec3i pos) {
        this.blockType = blockType;
        this.pos = pos;
    }

    /**
     * Returns a list of all available block types as strings
     * @return List of block type names
     */
    public static List<String> getAvailableBlockTypes() {
        return new ArrayList<>(BLOCK_MAP.keySet());
    }

    /**
     * Executes the action of spawning a specific block type at a predefined position within the game world.
     * Logs an error message if the block type is unknown or if the game world level cannot be determined.
     * The block is placed in the game world using the specified spawn block mode without causing block updates
     * or notifications to clients.
     *
     * @param server the MinecraftServer instance used to execute the action and retrieve the game level
     */
    @Override
    public void execute(MinecraftServer server) {
        var level = getLevel(server);
        if (level == null) {
            getLogger().severe("No level found.");
            return;
        }

        Block block = BLOCK_MAP.get(blockType);
        if (block == null) {
            getLogger().severe("Unknown block type " + blockType);
            return;
        }

        BlockPos safePos = PositionUtils.findNearestEmptyPosition(level, pos);
        if (safePos == null) {
            getLogger().severe("No safe position found for " + blockType + " at " + pos);
            return;
        }

        level.setBlock(safePos, block.defaultBlockState(), SPAWN_BLOCK_MODE);
    }
}
