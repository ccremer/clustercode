package clustercode.impl.scan;

import clustercode.api.domain.Media;
import clustercode.api.scan.FileScanner;
import clustercode.api.scan.MediaScanService;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XSlf4j
public class MediaScanServiceImpl implements MediaScanService {

    private final MediaScanConfig scanConfig;
    private final Provider<FileScanner> scannerProvider;

    @Inject
    MediaScanServiceImpl(MediaScanConfig scanConfig,
                         Provider<FileScanner> scannerProvider) {
        this.scanConfig = scanConfig;
        this.scannerProvider = scannerProvider;
    }

    @Override
    public Map<Path, List<Media>> retrieveFiles() {
        log.info("Scanning for directories in {}", scanConfig.base_input_dir());
        return scannerProvider.get()
            .searchIn(scanConfig.base_input_dir())
            .withRecursion(false)
            .withDirectories(true)
            .stream()
            .filter(this::isPriorityDirectory)
            .peek(path -> log.info("Found input directory: {}", path))
            .collect(Collectors.toMap(
                Function.identity(), this::getListOfMediaFiles));
    }

    @Override
    public List<Media> retrieveFilesAsList() {
        return retrieveFiles()
            .values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Collects a list of possible media candidates that are recursively found under the given path.
     *
     * @param path the path to the root directory.
     * @return a list of candidates which may empty on error or none found.
     */
    List<Media> getListOfMediaFiles(Path path) {
        log.info("Scanning for media files in {}", path);
        return scannerProvider.get()
            .searchIn(path)
            .withRecursion(true)
            .withFileExtensions(scanConfig.allowed_extensions())
            .whileSkippingExtraFilesWith(scanConfig.skip_extension_name())
            .whileSkippingExtraFilesIn(scanConfig.mark_source_dir())
            .streamAndIgnoreErrors()
            .map(file -> buildMedia(path, file))
            .peek(candidate -> log.info("Found file: {}", candidate))
            .collect(Collectors.toList());
    }

    /**
     * Creates a media object with the given priority dir and file location.
     *
     * @param priorityDir the root path, which must start with a number.
     * @param file        the file name, which will be relativized against the base input dir.
     * @return new media object.
     */
    Media buildMedia(Path priorityDir, Path file) {
        return Media.builder()
            .sourcePath(scanConfig.base_input_dir().relativize(file))
            .priority(getNumberFromDir(priorityDir))
            .build();
    }

    /**
     * Indicates whether the given path starts with a number {@literal >= 0}.
     *
     * @param path the path.
     * @return true if the filename is {@literal >= 0}.
     */
    boolean isPriorityDirectory(Path path) {
        try {
            return getNumberFromDir(path) >= 0;
        } catch (NumberFormatException ex) {
            log.debug(ex.getMessage());
            return false;
        }
    }

    /**
     * Get the number of the file path.
     *
     * @param path a relative path which starts with a number.
     * @return the number of the file name.
     * @throws NumberFormatException if the path could not be parsed and is not a number.
     */
    int getNumberFromDir(Path path) {
        return Integer.parseInt(path.getFileName().toString());
    }

}
