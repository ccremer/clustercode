package net.chrigel.clustercode.workflow.actions;

import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.workflow.StateController;
import net.chrigel.clustercode.workflow.states.WorkflowEventType;
import net.chrigel.clustercode.workflow.states.WorkflowState;

public class InitializeAction extends AbstractAction {


    @Override
    public void execute(WorkflowState from, WorkflowState to, WorkflowEventType event, StateContext context,
                        StateController stateMachine) {
        fireStateEvent(WorkflowEventType.FINISHED, stateMachine, context);
    }
}
