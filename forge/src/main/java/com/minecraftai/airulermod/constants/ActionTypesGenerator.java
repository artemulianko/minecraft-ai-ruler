package com.minecraftai.airulermod.constants;

import com.minecraftai.airulermod.actions.SpawnBlock;
import com.minecraftai.airulermod.actions.SpawnCreature;
import com.minecraftai.airulermod.actions.SpawnItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to generate TypeScript-style type definitions for available actions
 * based on the actual available block, creature, and item types.
 */
public class ActionTypesGenerator {
    private ActionTypesGenerator() {}

    /**
     * Generates the TypeScript-style type definition for available block types
     * @return A string like "MUD" | "WATER" | "LAVA" | ...
     */
    public static String generateBlockTypesString() {
        List<String> blockTypes = SpawnBlock.getAvailableBlockTypes();
        return blockTypes.stream()
                .map(type -> "\"" + type + "\"")
                .collect(Collectors.joining(" | "));
    }

    /**
     * Generates the TypeScript-style type definition for available creature types
     * @return A string like "COW" | "CHICKEN" | "CREEPER" | ...
     */
    public static String generateCreatureTypesString() {
        List<String> creatureTypes = SpawnCreature.getAvailableCreatureTypes();
        return creatureTypes.stream()
                .map(type -> "\"" + type + "\"")
                .collect(Collectors.joining(" | "));
    }
    
    /**
     * Generates the TypeScript-style type definition for available item types
     * @return A string like "DIAMOND_SWORD" | "GOLDEN_APPLE" | ...
     */
    public static String generateItemTypesString() {
        List<String> itemTypes = SpawnItem.getAvailableItemTypes();
        return itemTypes.stream()
                .map(type -> "\"" + type + "\"")
                .collect(Collectors.joining(" | "));
    }

    /**
     * Generates the complete actions description using the available types
     * @return The full action description string for the AI prompt
     */
    public static String generateActionsDescription() {
        return """
           Schema:
           {
             "actions": [
               {"type": "SpawnBlock", "blockType": %s, "pos": {"x": number, "y": number, "z": number}},
               {"type": "SpawnCreature", "creatureType": %s, "pos": {"x": number, "y": number, "z": number}},
               {"type": "SpawnItem", "itemType": %s, "pos": {"x": number, "y": number, "z": number}, "count": number},
               {"type": "SendMessage", "messageBody": string},
               {"type": "KickPlayer", "playerId": string, "reason": string},
               {"type": "MutePlayer", "playerId": string, "mute": boolean, "reason": string}
             ]
           }

           Return minified JSON only, no extra formatting or explanation. Respond with messages only when appropriate.
        """.formatted(
                generateBlockTypesString(),
                generateCreatureTypesString(),
                generateItemTypesString()
        );
    }
}