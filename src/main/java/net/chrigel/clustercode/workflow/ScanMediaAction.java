package net.chrigel.clustercode.workflow;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.MediaScanService;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.workflow.actions.AsyncAction;
import net.chrigel.clustercode.workflow.states.WorkflowEventType;
import net.chrigel.clustercode.workflow.states.WorkflowState;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@XSlf4j
public class ScanMediaAction extends AsyncAction<List<MediaCandidate>> {

    private final MediaScanService scanService;

    @Inject
    ScanMediaAction(MediaScanService scanService
    ) {
        this.scanService = scanService;
    }

    @Override
    protected Optional<List<MediaCandidate>> doExecute(WorkflowState from, WorkflowState to,
                                                       WorkflowEventType event,
                                                       StateContext context) {

        log.entry(from, to, event);
        Map<Path, List<MediaCandidate>> results = scanService.retrieveFiles();
        context.setCandidates(results);
        List<MediaCandidate> resultList = results.values().stream().flatMap(List::stream).collect(Collectors.toList());
        if (resultList.isEmpty()) {
            return log.exit(Optional.empty());
        } else {
            return log.exit(Optional.of(resultList));
        }
    }

}