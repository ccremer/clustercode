package clustercode.impl.scan;

import clustercode.api.config.converter.PathConverter;
import org.aeonbits.owner.Config;

import java.nio.file.Path;
import java.util.List;

public interface MediaScanConfig extends Config {

    /**
     * Gets the root directory for scanning.
     *
     * @return the root dir, not null.
     */
    @Key("CC_MEDIA_INPUT_DIR")
    @ConverterClass(PathConverter.class)
    @DefaultValue("/input")
    Path base_input_dir();

    /**
     * Gets the list of file name extensions. An entry can be ".txt" or "txt".
     *
     * @return the list of included file extensions. May be empty, not null.
     */
    @Key("CC_MEDIA_EXTENSIONS")
    @DefaultValue("mkv,mp4,avi")
    List<String> allowed_extensions();

    /**
     * Gets the extension which cause a file to be skipped in the scan. If e.g. the string equals ".doe", then a
     * file named "foo/bar" will be ignored if there is a file named "foo/bar.doe" in the same directory.
     *
     * @return the extension, not null.
     */
    @Key("CC_MEDIA_SKIP_NAME")
    @DefaultValue(".done")
    String skip_extension_name();

    /**
     * Gets the interval after which the file system is rescanned when no media has been found.
     *
     * @return the interval in minutes, >= 1.
     */
    @Key("CC_MEDIA_SCAN_INTERVAL")
    @DefaultValue("30")
    long media_scan_interval();

    /**
     * Gets the root path of the directory in which the sources should get marked as done.
     *
     * @return the path or empty.
     */
    @Key("CC_CLEANUP_MARK_SOURCE_DIR")
    @DefaultValue("/input/done")
    @ConverterClass(PathConverter.class)
    Path mark_source_dir();
}
