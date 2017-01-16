package net.chrigel.clustercode.workflow.actions;

import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.workflow.StateController;
import net.chrigel.clustercode.workflow.states.WorkflowEventType;
import net.chrigel.clustercode.workflow.states.WorkflowState;

import javax.inject.Inject;

public class InitializeAction extends AbstractAction {

    private final ClusterService clusterService;

    @Inject
    InitializeAction(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    public void execute(WorkflowState from, WorkflowState to, WorkflowEventType event, StateContext context,
                        StateController stateMachine) {

        clusterService.joinCluster();

        fireStateEvent(WorkflowEventType.FINISHED, stateMachine, context);
    }
}
