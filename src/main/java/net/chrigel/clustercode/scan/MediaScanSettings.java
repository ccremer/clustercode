package net.chrigel.clustercode.scan;

import java.nio.file.Path;
import java.util.List;

public interface MediaScanSettings {

    /**
     * Gets the root directory for scanning.
     *
     * @return the root dir, not null.
     */
    Path getBaseInputDir();

    /**
     * Gets the list of file name extensions. An entry can be ".txt" or "txt".
     *
     * @return the list of included file extensions. May be empty, not null.
     */
    List<String> getAllowedExtensions();

    /**
     * Gets the extension which cause a file to be skipped in the scan. If e.g. the string equals ".doe", then a
     * file named "foo/bar" will be ignored if there is a file named "foo/bar.doe" in the same directory.
     *
     * @return the extension, not null.
     */
    String getSkipExtension();

}
