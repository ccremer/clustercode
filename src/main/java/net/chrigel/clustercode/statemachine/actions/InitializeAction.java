package net.chrigel.clustercode.statemachine.actions;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;

@XSlf4j
public class InitializeAction extends Action {

    private final ClusterService clusterService;

    @Inject
    InitializeAction(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        log.info("Initializing state machine...");
        clusterService.joinCluster();

        if (clusterService.getSize() == 1) {
            log.info("We are the only member in the cluster. Let's wait 15 seconds before we continue");
            log.info("in order to make sure that there wasn't a connection problem and we can join");
            log.info("an existing cluster.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.warn(e.getMessage());
            }
        }

        return StateEvent.FINISHED;
    }
}
