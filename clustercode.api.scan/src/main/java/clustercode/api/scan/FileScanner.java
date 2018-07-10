package clustercode.api.scan;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a convenient file scanner which searches for files using filters. This class throws RuntimeExceptions
 * if it is being used incorrectly.
 */
public interface FileScanner {

    /**
     * Specifies the root directory of the scanner. This setting is mandatory.
     *
     * @param dir the root dir. Has to be a directory.
     * @return this.
     */
    FileScanner searchIn(Path dir);

    /**
     * Specifies whether subdirectories are being searched. This is equivalent to {@link #withDepth(Integer)} with a
     * depth of Integer.MAX_VALUE.
     *
     * @param recursive true if enabled.
     * @return this.
     */
    FileScanner withRecursion(boolean recursive);

    /**
     * Specifies the depth in which the directory should be searched. A value of 1 only scans the first level sub
     * directories. {@link Integer#MAX_VALUE} means all directories. This value overrides
     * {@link #withRecursion(boolean)}.
     *
     * @param value the depth value. Must be {@literal >= 1}.
     * @return this.
     */
    FileScanner withDepth(Integer value);

    /**
     * Enables the search for directories instead of files. All filters apply to directories instead of files too. By
     * default, this flag is set to false.
     *
     * @param dirs true if directory search should be enabled.
     * @return this.
     */
    FileScanner withDirectories(boolean dirs);

    /**
     * Sets the list of file extensions which are allowed to be included in the scan. Specify an entry with e.g. "
     * .txt" or "txt". If no matcher is provided, all sourcePath are being included.
     *
     * @param allowedExtensions the allowed extension list.
     * @return this.
     */
    FileScanner withFileExtensions(List<String> allowedExtensions);

    /**
     * Sets the extension which will cause e.g. the "foo/bar" file to be skipped if a sourcePath named
     * "foo/bar.skipping" exists too. This matcher is executed after {@link #withFileExtensions(List)}
     *
     * @param skipping the skipping extension.
     * @return this.
     */
    FileScanner whileSkippingExtraFilesWith(String skipping);

    /**
     * Sets the directory which is expected to contain the extra files for
     * {@link net.chrigel.clustercode.cleanup.processor.MarkSourceDirProcessor}. Needs {@link
     * #whileSkippingExtraFilesWith(String)} to be set.
     *
     * @param dir the directory. If it does not exist, it will be ignored.
     * @return this.
     */
    FileScanner whileSkippingExtraFilesIn(Path dir);

    /**
     * Scans the file system. This method blocks until the file system scan is complete. Any IO exception is being
     * logged as warning.
     *
     * @return A list of files which match the filters. Does not include the search dir. May be empty if no files
     * found. Returns {@link Optional#empty()} if the search could not be conducted.
     */
    Optional<List<Path>> scan();

    /**
     * Scans the file system using the settings in a stream-manner.
     *
     * @return the stream.
     * @throws RuntimeException if the stream could not be opened due to IO error with the original exception as
     *                          cause. The ex. is being logged as error.
     */
    Stream<Path> stream();

    /**
     * Scans the file system using a stream. Returns an empty stream if an IOException occurred (which is being
     * logged as warning).
     *
     * @return the (empty) stream.
     */
    Stream<Path> streamAndIgnoreErrors();
}
