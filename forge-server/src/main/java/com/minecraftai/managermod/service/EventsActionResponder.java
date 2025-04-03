package com.minecraftai.managermod.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftai.managermod.actions.AbstractAction;
import com.minecraftai.managermod.actions.SpawnCow;
import com.minecraftai.managermod.config.JsonConfig;
import com.minecraftai.managermod.events.AbstractGameEvent;

import java.util.List;

public class EventsActionResponder {
    private final static ObjectMapper objectMapper = JsonConfig.createObjectMapper();

    public static List<AbstractAction> respond(List<AbstractGameEvent> events) {
        try {
            String eventsJson = objectMapper.writeValueAsString(events);
            System.out.println(eventsJson);
            // Log or use the eventsJson as needed
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize events to JSON", e);
        }

        if (events.isEmpty()) {
            return List.of();
        }

        return List.of(
            new SpawnCow(events.getLast().getPos())
        );
    }
}
