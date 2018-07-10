package clustercode.api.scan;

import clustercode.api.domain.Media;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface MediaScanService {

    /**
     * Gets a map of files under the base input dir. Each key contains the sourcePath to a
     * directory
     * which contains a number as name. The number represents the priority of the queue. The sourcePath is relative to
     * the
     * base input directory. The value of the map is a list of files that were found in the sourcePath, with each
     * sourcePath
     * being relative to the base input directory. The non-null list is empty if no candidates for queueing were
     * found. Completed jobs are excluded from the list, as well as files which do not match the whitelisted
     * extensions.
     * <p>
     * Example:<br>
     * {@code /input/1/file_That_Is_Being_Included.mp4}
     * {@code /input/2/file_completed_will_be_ignored.mp4} because of
     * {@code /input/2/file_completed_will_be_ignored.mp4.done}
     * {@code /input/3/subdir/this_textfile_will_be_ignored.txt}
     * {@code /input/this_directory_is_not_a_number_and_will_be_ignored}<br>
     * The resulting map would contain 3 keys, but only key {@code /input/1} will have an entry in the list.
     * </p>
     * <p>
     * This method is blocking until the file system has been recursively scanned.
     * </p>
     *
     * @return the map as described. Empty map if no priority directories found.
     * @throws RuntimeException if base input dir is not readable.
     */
    Map<Path, List<Media>> retrieveFiles();

    /**
     * Gets a list of media files under base input dir. See {@link #retrieveFiles()}.
     * The resulting list would contain only {@code /input/1/file_That_Is_Being_Included.mp4} in the given example.
     * <p>
     * This method is blocking until the file system has been recursively scanned.
     * </p>
     *
     * @return the list as described. Empty list if no media files were found.
     * @throws RuntimeException if base input dir is not readable.
     */
    List<Media> retrieveFilesAsList();

}
