package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupStrategy;
import net.chrigel.clustercode.cleanup.impl.CleanupModule;
import net.chrigel.clustercode.util.di.ModuleHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConfigurableCleanupStrategy implements CleanupStrategy {

    private final List<CleanupProcessor> processors;

    @Inject
    ConfigurableCleanupStrategy(Set<CleanupProcessor> cleanupProcessors,
                                @Named(CleanupModule.CLEANUP_STRATEGY_KEY) String strategies) {
        this.processors = ModuleHelper.sortImplementations(strategies, cleanupProcessors, CleanupProcessors::valueOf);
    }

    @Override
    public Iterator<CleanupProcessor> processorIterator() {
        return processors.iterator();
    }
}
