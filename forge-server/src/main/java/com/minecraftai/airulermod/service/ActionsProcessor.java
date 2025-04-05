package com.minecraftai.airulermod.service;

import com.minecraftai.airulermod.actions.AbstractAction;
import com.minecraftai.airulermod.di.ServerHolder;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ActionsProcessor {
    private static final Logger LOGGER = Logger.getLogger(ActionsProcessor.class.getName());
    
    // Maximum actions to process in a single tick
    private static final int MAX_ACTIONS_PER_TICK = 50;
    
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
        LOGGER.info("Scheduled " + actions.size() + " new actions. Total pending: " + pendingActions.size());
    }

    /**
     * Processes actions from the queue at a controlled rate.
     * Processes at most MAX_ACTIONS_PER_TICK actions per server tick to avoid overwhelming the server.
     */
    public void processActions() {
        // If no actions, nothing to do
        if (pendingActions.isEmpty()) {
            return;
        }
        
        // Process a limited number of actions per tick
        int actionsProcessed = 0;
        while (!pendingActions.isEmpty() && actionsProcessed < MAX_ACTIONS_PER_TICK) {
            // Get the next action and execute it
            AbstractAction action = pendingActions.poll();
            if (action != null) {
                LOGGER.info("Executing action: " + action.getClass().getSimpleName());
                
                try {
                    // Execute the action
                    action.execute(serverHolder.getServer());
                    actionsProcessed++;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error executing action: " + action.getClass().getSimpleName(), e);
                }
            }
        }
        
        // Log the remaining actions
        if (!pendingActions.isEmpty()) {
            LOGGER.info("Remaining actions in queue: " + pendingActions.size());
        }
    }
}
