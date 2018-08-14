package clustercode.impl.scan;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.ClusterJoinedMessage;
import clustercode.api.event.messages.MediaScannedMessage;
import clustercode.api.event.messages.MediaSelectedMessage;
import clustercode.api.event.messages.ProfileSelectedMessage;
import clustercode.api.scan.MediaScanService;
import clustercode.api.scan.ProfileScanService;
import clustercode.api.scan.SelectionService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ScanServicesActivator implements Activator {

    private final RxEventBus eventBus;
    private final MediaScanService scanService;
    private final SelectionService selectionService;
    private final ProfileScanService profileScanService;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    public ScanServicesActivator(RxEventBus eventBus,
                                 MediaScanService scanService,
                                 SelectionService selectionService,
                                 ProfileScanService profileScanService) {
        this.eventBus = eventBus;
        this.scanService = scanService;
        this.selectionService = selectionService;
        this.profileScanService = profileScanService;
    }

    @Inject
    @Override
    public void activate(ActivatorContext context) {
        handlers.add(eventBus
                .register(ClusterJoinedMessage.class)
                .subscribe(msg -> onClusterJoined(), this::onError));
        handlers.add(eventBus
                .register(MediaScannedMessage.class)
                .subscribe(this::onMediaScanned));
        handlers.add(eventBus
                .register(MediaSelectedMessage.class)
                .filter(MediaSelectedMessage::isSelected)
                .map(MediaSelectedMessage::getMedia)
                .subscribe(this::onMediaSelected));
        handlers.add(eventBus
                .register(MediaSelectedMessage.class)
                .filter(MediaSelectedMessage::isNotSelected)
                .subscribe(msg -> {
                    log.info("No suitable media found. Either all media are already converted or the last one is " +
                            "being transcoded by a cluster member.");
                }));
        handlers.add(eventBus
                .register(ProfileSelectedMessage.class)
                .filter(ProfileSelectedMessage::isSelected)
                .subscribe(msg -> {
                    log.info("Selected {}", msg.getProfile());
                }));
        handlers.add(eventBus
                .register(ProfileSelectedMessage.class)
                .filter(ProfileSelectedMessage::isNotSelected)
                .subscribe(msg -> {

                }));
    }

    private void onError(Throwable throwable) {
        log.error("", throwable);
    }

    private void onMediaSelected(Media media) {
        log.info("Selected media: {}", media);
        Optional<Profile> result = profileScanService.selectProfile(media);
        eventBus.emitAsync(ProfileSelectedMessage
                .builder()
                .media(media)
                .profile(result.orElse(null))
                .build());
    }

    private void onMediaScanned(MediaScannedMessage msg) {
        log.debug("Selecting a suitable media for scheduling...");
        Optional<Media> result = selectionService.selectMedia(msg.getMediaList());
        eventBus.emitAsync(MediaSelectedMessage
                .builder()
                .media(result.orElse(null))
                .build());
    }

    private void onClusterJoined() {
        List<Media> resultList = scanService.retrieveFilesAsList();
        if (resultList.isEmpty()) {
            log.info("No media found.");
        } else {
            log.info("Found {} possible media entries.", resultList.size());
        }
        eventBus.emitAsync(MediaScannedMessage
                .builder()
                .mediaList(resultList)
                .build());
    }

    @Override
    public void deactivate(ActivatorContext context) {
        handlers.forEach(Disposable::dispose);
        handlers.clear();
    }
}
