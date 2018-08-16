package clustercode.impl.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.*;
import clustercode.api.scan.MediaScanService;
import clustercode.api.scan.ProfileScanService;
import clustercode.api.scan.SelectionService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
class ScanServicesMessageHandler {

    private final MediaScanService scanService;
    private final SelectionService selectionService;
    private final ProfileScanService profileScanService;
    private final RxEventBus eventBus;

    @Inject
    ScanServicesMessageHandler(
            MediaScanService scanService,
            SelectionService selectionService,
            ProfileScanService profileScanService,
            RxEventBus eventBus
    ) {
        this.scanService = scanService;
        this.selectionService = selectionService;
        this.profileScanService = profileScanService;
        this.eventBus = eventBus;
    }

    void onMediaScanRequest(ScanMediaCommand msg) {
        eventBus.emitAsync(MediaScannedMessage
                .builder()
                .mediaList(scanService.retrieveFilesAsList())
                .build());
    }

    void onSuccessfulMediaScan(MediaScannedMessage msg) {
        log.info("Found {} possible media entries.", msg.getMediaList().size());
        log.debug("Selecting a suitable media for scheduling...");
        Optional<Media> result = selectionService.selectMedia(msg.getMediaList());
        eventBus.emitAsync(MediaSelectedMessage
                .builder()
                .media(result.orElse(null))
                .build());
    }

    void onFailedMediaScan(MediaScannedMessage msg) {
        log.info("No media found.");
    }

    void onSuccessfulMediaSelection(Media media) {
        log.info("Selected media: {}", media);
        Optional<Profile> result = profileScanService.selectProfile(media);
        eventBus.emitAsync(ProfileSelectedMessage
                .builder()
                .media(media)
                .profile(result.orElse(null))
                .build());
    }

    void onFailedMediaSelection(MediaSelectedMessage msg) {
        log.info("No suitable media found. Either all media are already converted or the last one is " +
                "being transcoded by a cluster member.");
    }

    void onTimeout(Object msg) {
        startScanning();
    }

    private void startScanning() {
        eventBus.emitAsync(new ScanMediaCommand());
    }

    void onSuccessfulProfileSelection(ProfileSelectedMessage msg) {
        log.info("Selected {}", msg.getProfile());
    }

}
