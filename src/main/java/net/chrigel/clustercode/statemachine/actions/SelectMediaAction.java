package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.SelectionService;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;
import java.util.Optional;

public class SelectMediaAction extends Action {

    private final SelectionService selectionService;

    @Inject
    SelectMediaAction(SelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @Override
    public StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        log.debug("Selecting a suitable media for scheduling...");
        Optional<Media> result = selectionService.selectMedia(context.getCandidates());
        if (result.isPresent()) {
            log.info("Selected media: {}", result.get());
            context.setSelectedMedia(result.get());
            return StateEvent.RESULT;
        } else {
            log.info("No suitable media found.");
            return StateEvent.NO_RESULT;
        }
    }
}
