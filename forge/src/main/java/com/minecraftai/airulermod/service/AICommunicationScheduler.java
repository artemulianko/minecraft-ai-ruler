package com.minecraftai.airulermod.service;

import com.minecraftai.airulermod.actions.AbstractAction;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@Singleton
public class AICommunicationScheduler {
    private static final Logger LOGGER = Logger.getLogger(AICommunicationScheduler.class.getName());
    private static final long COMMUNICATION_INIT_DELAY = 5000;
    private static final long COMMUNICATION_RATE = 30000;

    private final EventsActionProcessor eventsActionProcessor;
    private final EventTracker eventTracker;
    private final StatsTracker statsService;
    private final ActionsProcessor actionsProcessor;

    private Timer timer;

    @Inject
    public AICommunicationScheduler(
            EventTracker eventTracker,
            StatsTracker statsService,
            EventsActionProcessor eventsActionProcessor,
            ActionsProcessor actionsProcessor
    ) {
        this.eventsActionProcessor = eventsActionProcessor;
        this.eventTracker = eventTracker;
        this.statsService = statsService;
        this.actionsProcessor = actionsProcessor;
    }

    /**
     * Initializes the communication scheduling mechanism by setting up a timer that periodically
     * triggers the {@code communicate()} method after a defined initial delay and at regular intervals.
     * The timer runs as a daemon thread, ensuring it does not prevent the application from shutting down.
     *
     * This method is typically called during the lifecycle of the class to kick-start the periodic communication process.
     */
    public void start() {
        LOGGER.info("Initializing AI communication scheduler");

        timer = new Timer("AICommunicationSchedulerTimer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                communicate();
            }
        }, COMMUNICATION_INIT_DELAY, COMMUNICATION_RATE);
    }

    /**
     * Stops the communication scheduling mechanism by canceling the active timer.
     */
    public void stop() {
        LOGGER.info("Stopping AI communication scheduler");

        timer.cancel();
    }

    private void communicate() {
        final List<AbstractAction> actionList = eventsActionProcessor.process(
                eventTracker.releaseEvents(),
                Map.of(
                        "miningRates", statsService.getAllMiningRates(),
                        "buildingRate", statsService.getAllBuildingRates()
                )
        );

        actionsProcessor.scheduleActions(actionList);
    }
}
