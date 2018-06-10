package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;

public class AddTaskInClusterAction extends Action {

    private ClusterService clusterService;

    @Inject
    AddTaskInClusterAction(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        clusterService.setTask(context.getSelectedMedia());
        return StateEvent.FINISHED;
    }
}
