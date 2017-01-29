package net.chrigel.clustercode.workflow;

import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.task.Media;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class WorkflowStateContext
        implements StateContext {


    private Map<Path, List<Media>> candidates;

    @Override
    public Map<Path, List<Media>> getCandidates() {
        return candidates;
    }

    @Override
    public void setCandidates(Map<Path, List<Media>> pathListMap) {
        this.candidates = Objects.requireNonNull(pathListMap);
    }
}
