package net.chrigel.clustercode.cleanup.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupService;
import net.chrigel.clustercode.cleanup.CleanupStrategy;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;

import javax.inject.Inject;
import java.util.Iterator;

@XSlf4j
class CleanupServiceImpl implements CleanupService {

    private final CleanupStrategy strategy;

    @Inject
    CleanupServiceImpl(CleanupStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void performCleanup(TranscodeFinishedEvent result) {

        CleanupContext context = CleanupContext.builder()
                .transcodeFinishedEvent(result)
                .build();

        log.info("Performing cleanup...");
        Iterator<CleanupProcessor> itr = strategy.processorIterator();
        while (itr.hasNext()) {
            context = itr.next().processStep(context);
        }
        log.info("Cleanup completed.");
    }
}
