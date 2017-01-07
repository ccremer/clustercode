package net.chrigel.clustercode.workflow.actions;

import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.workflow.StateController;
import net.chrigel.clustercode.workflow.states.WorkflowEventType;
import net.chrigel.clustercode.workflow.states.WorkflowState;
import org.squirrelframework.foundation.fsm.AnonymousAction;

public abstract class AbstractAction extends AnonymousAction<StateController, WorkflowState, WorkflowEventType,
        StateContext> {

    @Override
    public abstract void execute(WorkflowState from, WorkflowState to, WorkflowEventType event, StateContext
            context, StateController stateMachine);

    protected final void fireStateEvent(WorkflowEventType event, StateController stateMachine, StateContext context) {
        stateMachine.fire(event, context);
    }
}
