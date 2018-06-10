package net.chrigel.clustercode.cleanup;

import java.nio.file.Path;
import java.util.Optional;

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
    Optional<Integer> getGroupId();

    /**
     * Gets the user id of the new owner of the output file(s).
     */
    Optional<Integer> getUserId();

    /**
     * Gets the root path of the directory in which the sources should get marked as done.
     *
     * @return the path or empty.
     */
    Optional<Path> getMarkSourceDirectory();
}
