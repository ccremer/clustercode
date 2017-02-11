package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import java.util.concurrent.CompletableFuture;

public abstract class AsyncAction extends AbstractAction {

    @Override
    public final void execute(State from, State to, StateEvent event, StateContext context,
                              StateController stateMachine) {
        CompletableFuture.runAsync(() -> {
            StateEvent result = StateEvent.ERROR;
            try {
                result = doExecute(from, to, event, context);
            } catch (Exception ex) {
                log.error("catching:", ex);
            }
            fireStateEvent(result, stateMachine, context);
        });
    }

}
