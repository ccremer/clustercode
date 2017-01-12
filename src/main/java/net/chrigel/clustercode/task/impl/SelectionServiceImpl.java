package net.chrigel.clustercode.task.impl;

import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.task.SelectionService;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SelectionServiceImpl implements SelectionService {

    private final Set<Constraint> constraints;

    @Inject
    SelectionServiceImpl(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public Optional<MediaCandidate> selectJob(List<MediaCandidate> list) {
        return list.stream()
                .sorted(Comparator.comparingInt(MediaCandidate::getPriority).reversed())
                .filter(this::checkConstraints)
                .findFirst();
    }

    private boolean checkConstraints(MediaCandidate mediaCandidate) {
        return constraints.stream().allMatch(filter -> filter.accept(mediaCandidate));
    }

}
