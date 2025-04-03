package com.minecraftai.managermod.service;
import com.minecraftai.managermod.events.AbstractGameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventTracker {
    private EventTracker() {}

    public static Queue<AbstractGameEvent> events = new ConcurrentLinkedQueue<>();

    public static void track(AbstractGameEvent event) {
        events.add(event);
    }

    public static List<AbstractGameEvent> releaseEvents() {
        List<AbstractGameEvent> eventList = new LinkedList<>();

        while (!events.isEmpty()) {
            eventList.add(events.poll());
        }

        return eventList;

    }
}
