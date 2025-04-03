package com.minecraftai.managermod.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SpawnBlock extends AbstractAction {
    private final String blockType;
    private final Vec3i pos;

    /**
     * Means:
     * 1. - Only make the change visible to clients without causing block updates
     * 2. - Only notify neighbors without updating clients (rarely used alone)
     */
    private static int SPAWN_BLOCK_MODE = 3;

    public SpawnBlock(String blockType, Vec3i pos) {
        this.blockType = blockType;
        this.pos = pos;
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "No level found.");
            return;
        }

        final var block = switch (blockType) {
            case "MUD" -> Blocks.MUD.defaultBlockState();
            case "WATER" -> Blocks.WATER.defaultBlockState();
            case "LAVA" -> Blocks.LAVA.defaultBlockState();
            case "ANVIL" -> Blocks.ANVIL.defaultBlockState();
            case "TNT" -> Blocks.TNT.defaultBlockState();
            // Add other block types here
            default -> null;
        };

        if (block == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unknown block type " + blockType);
            return;
        }

        level.setBlock(new BlockPos(pos), block, SPAWN_BLOCK_MODE);
    }
}
