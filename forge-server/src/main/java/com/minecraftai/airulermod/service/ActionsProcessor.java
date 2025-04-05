package com.minecraftai.airulermod.service;

import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.di.ServerHolder;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class ActionsProcessor {
    private final ServerHolder serverHolder;
    private final Queue<AbstractAction> pendingActions = new ConcurrentLinkedQueue<>();

    @Inject
    public ActionsProcessor(ServerHolder serverHolder) {
        this.serverHolder = serverHolder;
    }

    /**
     * Adds a list of actions to the pending actions queue. These actions will be
     * processed sequentially when the `processActions` method is called.
     */
    public void scheduleActions(List<AbstractAction> actions) {
        pendingActions.addAll(actions);
    }

    /**
     * Processes and executes all pending actions in the queue. Each action is retrieved from
     * the queue and executed sequentially in the order it was added. Execution is performed
     * using the server instance provided to this processor.
     */
    public void processActions() {
        AbstractAction action;

        while ((action = pendingActions.poll()) != null) {
            action.execute(serverHolder.getServer());
        }
    }
}
