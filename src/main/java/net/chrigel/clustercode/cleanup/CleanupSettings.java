package net.chrigel.clustercode.cleanup;

import java.nio.file.Path;

public interface CleanupSettings {

    /**
     * Gets the root path of the output directory.
     *
     * @return the path, not null.
     */
    Path getOutputBaseDirectory();

    /**
     * Returns true if existing files are allowed to be overwritten.
     */
    boolean overwriteFiles();

    /**
     * Gets the group id of the new owner of the output file(s).
     */
    int getGroupId();

    /**
     * Gets the user id of the new owner of the output file(s).
     */
    int getUserId();
}
