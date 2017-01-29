package net.chrigel.clustercode.task.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.task.Media;
import net.chrigel.clustercode.task.SelectionService;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@XSlf4j
public class SelectionServiceImpl implements SelectionService {

    private final Set<Constraint> constraints;
    private final ClusterService clusterService;

    @Inject
    SelectionServiceImpl(Set<Constraint> constraints,
                         ClusterService clusterService) {
        this.constraints = constraints;
        this.clusterService = clusterService;
    }

    @Override
    public Optional<Media> selectMedia(List<Media> list) {
        return log.exit(list.stream()
                .sorted(Comparator.comparingInt(Media::getPriority).reversed())
                .filter(this::checkConstraints)
                .filter(this::isNotInCluster)
                .findFirst());
    }

    /**
     * Checks whether the given media candidate is already queued in the cluster.
     *
     * @param candidate the media. Not null.
     * @return true if queued.
     */
    boolean isInCluster(Media candidate) {
        return clusterService.isQueuedInCluster(candidate);
    }

    /**
     * Negates {@link #isInCluster(Media)}.
     *
     * @param candidate
     * @return
     */
    boolean isNotInCluster(Media candidate) {
        return !isInCluster(candidate);
    }

    /**
     * Checks whether the given media candidate fulfills all constraints.
     *
     * @param media
     * @return
     */
    boolean checkConstraints(Media media) {
        return constraints.stream().allMatch(filter -> filter.accept(media));
    }

}
