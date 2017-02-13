package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.squirrelframework.foundation.fsm.AnonymousAction;

public abstract class Action extends AnonymousAction<StateController, State, StateEvent, StateContext> {

    protected final XLogger log = XLoggerFactory.getXLogger(getClass());

    @Override
    public void execute(State from, State to, StateEvent event, StateContext
            context, StateController stateMachine) {
        fireStateEvent(doExecute(from, to, event, context), stateMachine, context);
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
}
