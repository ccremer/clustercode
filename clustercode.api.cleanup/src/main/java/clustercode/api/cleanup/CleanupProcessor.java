package clustercode.api.cleanup;

public interface CleanupProcessor {

    CleanupContext processStep(CleanupContext context);

}
