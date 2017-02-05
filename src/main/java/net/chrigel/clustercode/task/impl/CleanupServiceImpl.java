package net.chrigel.clustercode.task.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.task.CleanupContext;
import net.chrigel.clustercode.task.CleanupProcessor;
import net.chrigel.clustercode.task.CleanupService;
import net.chrigel.clustercode.task.CleanupStrategy;
import net.chrigel.clustercode.transcode.TranscodeResult;

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
    public void performCleanup(TranscodeResult result) {

        CleanupContext context = CleanupContext.builder()
                .transcodeResult(result)
                .build();

        log.info("Performing cleanup...");
        Iterator<CleanupProcessor> itr = strategy.processorIterator();
        while (itr.hasNext()) {
            context = itr.next().processStep(context);
        }
        log.info("Cleanup completed.");
    }
}
