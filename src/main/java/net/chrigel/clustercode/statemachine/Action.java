package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.api.StateMachineMonitor;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateController;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.squirrelframework.foundation.fsm.AnonymousAction;

import javax.inject.Inject;

public abstract class Action extends AnonymousAction<StateController, State, StateEvent, StateContext> {

    protected final XLogger log = XLoggerFactory.getXLogger(getClass());

    @Inject
    private StateMachineMonitor stateMachineMonitor;

    @Override
    public void execute(State from, State to, StateEvent event, StateContext
            context, StateController stateMachine) {
        fireStateEvent(doExecute(from, to, event, context), stateMachine, context);
        updateCurrentState(to);
    }

    /**
     * This method is being executed asynchronously.
     *
     * @param from
     * @param to
     * @param event   the firing event type.
     * @param context the state machine context
     * @return the state event after finishing.
     */
    protected abstract StateEvent doExecute(State from, State to, StateEvent event, StateContext context);

    protected final void fireStateEvent(StateEvent event, StateController stateMachine, StateContext context) {
        stateMachine.fire(event, context);
    }

    protected final void updateCurrentState(State state) {
        if (stateMachineMonitor == null) return;
        stateMachineMonitor.setCurrentState(state);
    }
}
