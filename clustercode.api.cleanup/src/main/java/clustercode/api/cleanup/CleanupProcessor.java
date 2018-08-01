package clustercode.api.cleanup;

import clustercode.impl.util.Indexable;

public interface CleanupProcessor extends Indexable<CleanupProcessor> {

    CleanupContext processStep(CleanupContext context);

}
