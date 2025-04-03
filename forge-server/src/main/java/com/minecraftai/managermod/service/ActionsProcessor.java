package com.minecraftai.managermod.service;

import com.minecraftai.managermod.actions.AbstractAction;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionsProcessor {
    private static MinecraftServer server;
    private static final Queue<AbstractAction> pendingActions = new ConcurrentLinkedQueue<>();

    public static void init(MinecraftServer server) {
        ActionsProcessor.server = server;
    }

    public static void scheduleActions(List<AbstractAction> actions) {
        pendingActions.addAll(actions);
    }

    public static void processActions() {
        AbstractAction action;

        while ((action = pendingActions.poll()) != null) {
            action.execute(server);
        }
    }
}
