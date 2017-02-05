package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupStrategy;
import net.chrigel.clustercode.util.di.ModuleHelper;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConfigurableCleanupStrategy implements CleanupStrategy {

    private final List<CleanupProcessor> processors;

    @Inject
    ConfigurableCleanupStrategy(Set<CleanupProcessor> cleanupProcessors) {
        this.processors = ModuleHelper.sortImplementations("key", cleanupProcessors, CleanupProcessors::valueOf);
    }

    @Override
    public Iterator<CleanupProcessor> processorIterator() {
        return processors.iterator();
    }
}
