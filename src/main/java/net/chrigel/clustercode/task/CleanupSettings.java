package net.chrigel.clustercode.task;

import java.nio.file.Path;

public interface CleanupSettings {

    /**
     * Gets the root path of the output directory.
     *
     * @return the path, not null.
     */
    Path getOutputBaseDirectory();

    boolean overwriteFiles();
}
