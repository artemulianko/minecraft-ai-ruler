package com.minecraftai.airulermod.service;
import com.minecraftai.airulermod.events.AbstractGameEvent;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class EventTracker {
    @Inject
    public EventTracker() {}

    public static Queue<AbstractGameEvent> events = new ConcurrentLinkedQueue<>();

    public void track(AbstractGameEvent event) {
        events.add(event);
    }

    /**
     * Releases all recorded game events from the event queue and returns them as a list.
     * Once retrieved, the events are removed from the queue, and subsequent calls to this method
     * will only return new events added after the previous release.
     */
    public List<AbstractGameEvent> releaseEvents() {
        List<AbstractGameEvent> eventList = new LinkedList<>();

        while (!events.isEmpty()) {
            eventList.add(events.poll());
        }

        return eventList;

    }
}
