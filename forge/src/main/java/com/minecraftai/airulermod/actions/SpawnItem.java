package com.minecraftai.airulermod.actions;

import com.minecraftai.airulermod.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Action to spawn an item in the world at a specified position.
 */
public class SpawnItem extends AbstractAction {
    public static final String ACTION_TYPE = "SpawnItem";
    
    // Map of available item types
    private static final Map<String, Item> ITEM_MAP = Map.ofEntries(
        // Tools & Weapons
        Map.entry("DIAMOND_SWORD", Items.DIAMOND_SWORD),
        Map.entry("DIAMOND_PICKAXE", Items.DIAMOND_PICKAXE),
        Map.entry("DIAMOND_AXE", Items.DIAMOND_AXE),
        Map.entry("BOW", Items.BOW),
        Map.entry("CROSSBOW", Items.CROSSBOW),
        Map.entry("TRIDENT", Items.TRIDENT),
        Map.entry("SHIELD", Items.SHIELD),
        
        // Armor
        Map.entry("DIAMOND_HELMET", Items.DIAMOND_HELMET),
        Map.entry("DIAMOND_CHESTPLATE", Items.DIAMOND_CHESTPLATE),
        Map.entry("DIAMOND_LEGGINGS", Items.DIAMOND_LEGGINGS),
        Map.entry("DIAMOND_BOOTS", Items.DIAMOND_BOOTS),
        
        // Food
        Map.entry("COOKED_BEEF", Items.COOKED_BEEF),
        Map.entry("GOLDEN_APPLE", Items.GOLDEN_APPLE),
        Map.entry("ENCHANTED_GOLDEN_APPLE", Items.ENCHANTED_GOLDEN_APPLE),
        Map.entry("CAKE", Items.CAKE),
        
        // Materials
        Map.entry("DIAMOND", Items.DIAMOND),
        Map.entry("EMERALD", Items.EMERALD),
        Map.entry("GOLD_INGOT", Items.GOLD_INGOT),
        Map.entry("IRON_INGOT", Items.IRON_INGOT),
        Map.entry("NETHERITE_INGOT", Items.NETHERITE_INGOT),
        
        // Special items
        Map.entry("ENDER_PEARL", Items.ENDER_PEARL),
        Map.entry("EXPERIENCE_BOTTLE", Items.EXPERIENCE_BOTTLE),
        Map.entry("FIREWORK_ROCKET", Items.FIREWORK_ROCKET),
        Map.entry("TOTEM_OF_UNDYING", Items.TOTEM_OF_UNDYING),
        Map.entry("ELYTRA", Items.ELYTRA),
        
        // Potions
        Map.entry("POTION", Items.POTION),
        Map.entry("SPLASH_POTION", Items.SPLASH_POTION),
        Map.entry("LINGERING_POTION", Items.LINGERING_POTION),
        
        // Misc
        Map.entry("ENDER_CHEST", Items.ENDER_CHEST),
        Map.entry("TORCH", Items.TORCH)
    );
    
    private final String itemType;
    private final Vec3i pos;
    private final int count;
    
    /**
     * Creates a new SpawnItem action
     * 
     * @param itemType The type of item to spawn
     * @param pos The position where the item should appear
     * @param count The number of items in the stack (1-64)
     */
    public SpawnItem(String itemType, Vec3i pos, int count) {
        this.itemType = itemType;
        this.pos = pos;
        this.count = Math.max(1, Math.min(64, count)); // Ensure count is between 1 and 64
    }
    
    /**
     * Returns a list of all available item types
     * @return List of item type names
     */
    public static List<String> getAvailableItemTypes() {
        return new ArrayList<>(ITEM_MAP.keySet());
    }
    
    @Override
    public void execute(MinecraftServer server) {
        var level = getLevel(server);
        if (level == null) {
            getLogger().severe("No level found.");
            return;
        }

        Item item = ITEM_MAP.get(itemType);
        if (item == null) {
            getLogger().severe("Unknown item type: " + itemType);
            return;
        }

        BlockPos safePos = PositionUtils.findNearestEmptyPosition(level, pos);
        if (safePos == null) {
            getLogger().severe("No safe position found for " + itemType + " at " + pos);
            return;
        }

        ItemStack itemStack = new ItemStack(item, count);
        ItemEntity itemEntity = new ItemEntity(
            level, 
            safePos.getX() + 0.5, 
            safePos.getY() + 0.5, 
            safePos.getZ() + 0.5, 
            itemStack
        );
        
        // Set some reasonable velocity for the item
        itemEntity.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(itemEntity);
    }
}