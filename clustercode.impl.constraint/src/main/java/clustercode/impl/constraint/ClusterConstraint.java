package clustercode.impl.constraint;

import clustercode.api.domain.Media;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.MediaInClusterMessage;

import javax.inject.Inject;

public class ClusterConstraint extends AbstractConstraint {

    private final RxEventBus eventBus;

    @Inject
    ClusterConstraint(RxEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public boolean accept(Media candidate) {
        boolean isInCluster = eventBus.emit(
                MediaInClusterMessage.builder()
                                     .media(candidate)
                                     .build()
        ).isInCluster();
        return logAndReturnResult(!isInCluster, "{} is in cluster: {}", candidate.getSourcePath(), isInCluster);
    }
}
