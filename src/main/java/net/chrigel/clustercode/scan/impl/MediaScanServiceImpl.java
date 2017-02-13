package net.chrigel.clustercode.scan.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.FileScanner;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanService;
import net.chrigel.clustercode.scan.MediaScanSettings;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XSlf4j
class MediaScanServiceImpl implements MediaScanService {

    private final MediaScanSettings scanSettings;
    private final Provider<FileScanner> scannerProvider;

    @Inject
    MediaScanServiceImpl(MediaScanSettings scanSettings, Provider<FileScanner> scannerProvider) {
        this.scanSettings = scanSettings;
        this.scannerProvider = scannerProvider;
    }

    @Override
    public Map<Path, List<Media>> retrieveFiles() {
        log.info("Scanning for directories in {}", scanSettings.getBaseInputDir());
        return scannerProvider.get()
                .searchIn(scanSettings.getBaseInputDir())
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
                .withFileExtensions(scanSettings.getAllowedExtensions())
                .whileSkippingExtraFilesWith(scanSettings.getSkipExtension())
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
                .sourcePath(scanSettings.getBaseInputDir().relativize(file))
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
            log.catching(XLogger.Level.DEBUG, ex);
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
