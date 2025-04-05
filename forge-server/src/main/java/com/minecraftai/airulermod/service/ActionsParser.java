package com.minecraftai.airulermod.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.actions.SendMessage;
import com.minecraftai.airulermod.actions.SpawnBlock;
import com.minecraftai.airulermod.actions.SpawnCreature;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ActionsParser {
    private final Gson serializer;

    @Inject
    public ActionsParser(Gson serializer) {
        this.serializer = serializer;
    }

    public AbstractAction parse(JsonObject actionJson) {
        String actionType = actionJson.get("type").getAsString();

        try {
            return switch (actionType) {
                case SpawnCreature.ACTION_TYPE -> serializer.fromJson(actionJson, SpawnCreature.class);
                case SpawnBlock.ACTION_TYPE -> serializer.fromJson(actionJson, SpawnBlock.class);
                case SendMessage.ACTION_TYPE -> serializer.fromJson(actionJson, SendMessage.class);

                default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
            };
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to deserialize action: " + actionJson);
        }

        return null;
    }
}
