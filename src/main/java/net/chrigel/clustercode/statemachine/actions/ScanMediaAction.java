package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanService;
import net.chrigel.clustercode.statemachine.AsyncAction;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;
import java.util.List;

public class ScanMediaAction extends AsyncAction {

    private final MediaScanService scanService;

    @Inject
    ScanMediaAction(MediaScanService scanService) {
        this.scanService = scanService;
    }

    @Override
    public StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {

        log.entry(from, to, event, context);
        log.info("Scanning for media files...");
        List<Media> resultList = scanService.retrieveFilesAsList();
        context.setCandidates(resultList);
        if (resultList.isEmpty()) {
            log.info("No media found.");
            return StateEvent.NO_RESULT;
        } else {
            log.info("Found {} possible media entries.", resultList.size());
            return StateEvent.RESULT;
        }
    }
}