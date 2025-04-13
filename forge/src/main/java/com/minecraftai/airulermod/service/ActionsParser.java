package com.minecraftai.airulermod.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecraftai.airulermod.actions.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ActionsParser {
    private static final Logger LOGGER = Logger.getLogger(ActionsParser.class.getName());
    
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
                case SpawnItem.ACTION_TYPE -> serializer.fromJson(actionJson, SpawnItem.class);
                case SendMessage.ACTION_TYPE -> serializer.fromJson(actionJson, SendMessage.class);
                case KickPlayer.ACTION_TYPE -> serializer.fromJson(actionJson, KickPlayer.class);
                case MutePlayer.ACTION_TYPE -> serializer.fromJson(actionJson, MutePlayer.class);

                default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
            };
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize action: " + actionJson, e);
        }

        return null;
    }
}
