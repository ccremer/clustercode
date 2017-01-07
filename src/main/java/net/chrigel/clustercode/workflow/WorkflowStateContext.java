package net.chrigel.clustercode.workflow;

import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.task.MediaCandidate;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class WorkflowStateContext
        implements StateContext {


    private Map<Path, List<MediaCandidate>> candidates;

    @Override
    public Map<Path, List<MediaCandidate>> getCandidates() {
        return candidates;
    }

    @Override
    public void setCandidates(Map<Path, List<MediaCandidate>> pathListMap) {
        this.candidates = Objects.requireNonNull(pathListMap);
    }
}
