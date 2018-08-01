package clustercode.impl.cleanup;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.cleanup.CleanupProcessor;
import clustercode.api.cleanup.CleanupService;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.processor.CleanupProcessors;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Map;

@Slf4j
public class CleanupServiceImpl implements CleanupService {

    private final Map<CleanupProcessors, CleanupProcessor> cleanupProcessorMap;

    @Inject
    CleanupServiceImpl(Map<CleanupProcessors, CleanupProcessor> cleanupProcessorMap) {
        this.cleanupProcessorMap = cleanupProcessorMap;
    }

    @Override
    public void performCleanup(TranscodeFinishedEvent result) {

        CleanupContext context = CleanupContext.builder()
                                               .transcodeFinishedEvent(result)
                                               .build();

        log.info("Performing cleanup...");
        for (CleanupProcessor cleanupProcessor : cleanupProcessorMap.values()) {
            context = cleanupProcessor.processStep(context);
        }
        log.info("Cleanup completed.");
    }
}
