package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.AbstractAction;
import net.chrigel.clustercode.statemachine.StateController;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;

public class InitializeAction extends AbstractAction {

    private final ClusterService clusterService;

    @Inject
    InitializeAction(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    public void execute(State from, State to, StateEvent event, StateContext context,
                        StateController stateMachine) {
        fireStateEvent(doExecute(from, to, event, context), stateMachine, new StateContext());
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        clusterService.joinCluster();

        return StateEvent.FINISHED;
    }
}
