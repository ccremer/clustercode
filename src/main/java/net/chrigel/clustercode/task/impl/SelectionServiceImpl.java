package net.chrigel.clustercode.task.impl;

import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.task.SelectionService;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.*;

public class SelectionServiceImpl implements SelectionService {

    private final Set<Constraint> constraints;

    @Inject
    SelectionServiceImpl(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public Optional<MediaCandidate> selectJob(Map<Path, List<MediaCandidate>> listMap) {
        return listMap.entrySet()
                .stream()
                .filter(pathListEntry -> pathListEntry.getValue().isEmpty())
                .flatMap(pathListEntry -> pathListEntry.getValue().stream())
                .sorted(Comparator.comparingInt(MediaCandidate::getPriority).reversed())
                .filter(this::checkConstraints)
                .findFirst();
    }

    private boolean checkConstraints(MediaCandidate mediaCandidate) {
        return constraints.stream().allMatch(filter -> filter.accept(mediaCandidate));
    }

}
