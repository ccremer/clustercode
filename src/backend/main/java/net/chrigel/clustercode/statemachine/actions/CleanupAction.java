package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.cleanup.CleanupService;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;

public class CleanupAction extends Action {

    private final CleanupService cleanupService;

    @Inject
    CleanupAction(CleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        cleanupService.performCleanup(context.getTranscodeFinishedEvent());
        return StateEvent.FINISHED;
    }
}
