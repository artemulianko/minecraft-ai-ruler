package com.minecraftai.airulermod.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Utility class for working with positions in the Minecraft world
 */
public class PositionUtils {
    private static final Logger LOGGER = Logger.getLogger(PositionUtils.class.getName());
    
    // Maximum search radius for finding an empty block position
    private static final int MAX_SEARCH_RADIUS = 5;
    
    // Define the directions to search in (up, down, north, south, east, west)
    private static final int[][] DIRECTIONS = {
        {0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}
    };
    
    private PositionUtils() {}
    
    /**
     * Finds the nearest empty block position to the given position
     * Uses a breadth-first search algorithm to find the closest empty position
     * 
     * @param level The game world level
     * @param pos The original target position
     * @return The nearest empty block position, or the original position if already empty
     */
    public static BlockPos findNearestEmptyPosition(Level level, Vec3i pos) {
        BlockPos originalPos = new BlockPos(pos);
        
        // If the original position is already empty, return it
        if (isPositionSafe(level, originalPos)) {
            return originalPos;
        }
        
        // Use a breadth-first search to find the closest empty position
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(originalPos);
        
        // Keep track of visited positions to avoid duplicates
        Queue<BlockPos> visited = new LinkedList<>();
        visited.add(originalPos);
        
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            
            // Check each adjacent position
            for (int[] dir : DIRECTIONS) {
                BlockPos adjacent = current.offset(dir[0], dir[1], dir[2]);
                
                // Skip if we've already checked this position
                if (visited.contains(adjacent)) {
                    continue;
                }
                
                visited.add(adjacent);
                
                // If this position is safe, return it
                if (isPositionSafe(level, adjacent)) {
                    LOGGER.info("Found safe position at " + adjacent + " (original was " + originalPos + ")");
                    return adjacent;
                }
                
                // If we haven't exceeded our search radius, add this position to the queue
                if (distanceTo(originalPos, adjacent) <= MAX_SEARCH_RADIUS) {
                    queue.add(adjacent);
                }
            }
        }
        
        // If we can't find a suitable position, return the original
        LOGGER.warning("Could not find empty position near " + originalPos);
        return originalPos;
    }
    
    /**
     * Checks if a position is safe to place a block or spawn an entity
     * 
     * @param level The game world level
     * @param pos The position to check
     * @return true if the position is safe, false otherwise
     */
    public static boolean isPositionSafe(Level level, BlockPos pos) {
        // Check if the block at this position is replaceable (air, water, etc.)
        return level.getBlockState(pos).isAir() || 
               level.getBlockState(pos).canBeReplaced();
    }
    
    /**
     * Calculates the Manhattan distance between two positions
     * 
     * @param pos1 The first position
     * @param pos2 The second position
     * @return The Manhattan distance between the positions
     */
    private static int distanceTo(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + 
               Math.abs(pos1.getY() - pos2.getY()) + 
               Math.abs(pos1.getZ() - pos2.getZ());
    }
}