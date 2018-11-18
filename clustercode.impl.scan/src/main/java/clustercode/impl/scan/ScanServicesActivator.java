package clustercode.impl.scan;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.*;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ScanServicesActivator implements Activator {

    private final RxEventBus eventBus;
    private final List<Disposable> handlers = new LinkedList<>();
    private final ScanServicesMessageHandler messageHandler;
    private final MediaScanConfig config;

    @Inject
    public ScanServicesActivator(RxEventBus eventBus,
                                 ScanServicesMessageHandler messageHandler,
                                 MediaScanConfig config
    ) {
        this.eventBus = eventBus;
        this.messageHandler = messageHandler;
        this.config = config;
    }

    @Override
    public void preActivate(ActivatorContext context) {
        log.debug("Activating scanning services.");
        handlers.add(eventBus
                .listenFor(ClusterConnectMessage.class)
                .map(msg -> new ScanMediaCommand())
                .subscribe(messageHandler::onMediaScanRequest));
        handlers.add(eventBus
                .listenFor(CleanupFinishedMessage.class)
                .map(msg -> new ScanMediaCommand())
                .subscribe(messageHandler::onMediaScanRequest));
        handlers.add(eventBus
                .listenFor(ScanMediaCommand.class)
                .subscribe(messageHandler::onMediaScanRequest));
        handlers.add(eventBus
                .listenFor(MediaScannedMessage.class)
                .filter(MediaScannedMessage::listHasEntries)
                .subscribe(messageHandler::onSuccessfulMediaScan));
        handlers.add(eventBus
                .listenFor(MediaScannedMessage.class)
                .filter(MediaScannedMessage::listIsEmpty)
                .subscribe(messageHandler::onFailedMediaScan));
        handlers.add(eventBus
                .listenFor(MediaSelectedMessage.class)
                .filter(MediaSelectedMessage::isSelected)
                .map(MediaSelectedMessage::getMedia)
                .subscribe(messageHandler::onSuccessfulMediaSelection));
        handlers.add(eventBus
                .listenFor(ProfileSelectedMessage.class)
                .filter(ProfileSelectedMessage::isSelected)
                .subscribe(messageHandler::onSuccessfulProfileSelection));
        handlers.add(eventBus
                .listenFor(MediaSelectedMessage.class)
                .filter(MediaSelectedMessage::isNotSelected)
                .subscribe(messageHandler::onFailedMediaSelection));
        handlers.add(eventBus
                .listenFor(ProfileSelectedMessage.class)
                .filter(ProfileSelectedMessage::isNotSelected)
                .map(this::onWaiting)
                .delay(config.media_scan_interval(), TimeUnit.MINUTES)
                .subscribe(messageHandler::onTimeout));
        handlers.add(eventBus
                .listenFor(MediaSelectedMessage.class)
                .filter(MediaSelectedMessage::isNotSelected)
                .map(this::onWaiting)
                .delay(config.media_scan_interval(), TimeUnit.MINUTES)
                .subscribe(messageHandler::onTimeout));
        handlers.add(eventBus
                .listenFor(MediaScannedMessage.class)
                .filter(MediaScannedMessage::listIsEmpty)
                .map(this::onWaiting)
                .delay(config.media_scan_interval(), TimeUnit.MINUTES)
                .subscribe(messageHandler::onTimeout));
    }

    @Override
    public void activate(ActivatorContext context) {
    }

    private <T> T onWaiting(T msg) {
        log.info("Waiting {} minutes.", config.media_scan_interval());
        return msg;
    }

    @Override
    public void deactivate(ActivatorContext context) {
        log.debug("Deactivating scanning services.");
        handlers.forEach(Disposable::dispose);
        handlers.clear();
    }
}
