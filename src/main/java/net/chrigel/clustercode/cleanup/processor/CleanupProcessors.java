package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum CleanupProcessors implements EnumeratedImplementation<CleanupProcessor> {

    UNIFIED_OUTPUT(UnifiedOutputDirectoryProcessor.class),
    STRUCTURED_OUTPUT(StructuredOutputDirectoryProcessor.class),
    DELETE_SOURCE(DeleteSourceProcessor.class),
    MARK_SOURCE(MarkSourceProcessor.class);

    private final Class<? extends CleanupProcessor> implementingClass;

    CleanupProcessors(Class<? extends CleanupProcessor> implementingClass) {
        this.implementingClass = implementingClass;
    }

    @Override
    public Class<? extends CleanupProcessor> getImplementingClass() {
        return implementingClass;
    }
}
