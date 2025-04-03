package com.minecraftai.managermod.service;
import com.minecraftai.managermod.events.AbstractGameEvent;
import jakarta.inject.Inject;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
