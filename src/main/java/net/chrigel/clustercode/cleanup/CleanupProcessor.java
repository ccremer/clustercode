package net.chrigel.clustercode.cleanup;

public interface CleanupProcessor {


    CleanupContext processStep(CleanupContext context);

}
