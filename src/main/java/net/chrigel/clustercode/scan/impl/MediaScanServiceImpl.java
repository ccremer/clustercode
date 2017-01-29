package net.chrigel.clustercode.scan.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.FileScanner;
import net.chrigel.clustercode.scan.MediaScanService;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.task.Media;
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

    List<Media> getListOfMediaFiles(Path path) {
        return scannerProvider.get()
                .searchIn(path)
                .withRecursion(true)
                .withFileExtensions(scanSettings.getAllowedExtensions())
                .whileSkippingExtraFilesWith(scanSettings.getSkipExtension())
                .streamAndIgnoreErrors()
                .map(file -> Media.builder()
                        .sourcePath(scanSettings.getBaseInputDir().relativize(file))
                        .priority(getNumberFromDir(path))
                        .build()
                )
                .peek(candidate -> log.info("Found file: {}", candidate))
                .collect(Collectors.toList());
    }

    boolean isPriorityDirectory(Path path) {
        try {
            return getNumberFromDir(path) >= 0;
        } catch (NumberFormatException ex) {
            log.catching(XLogger.Level.DEBUG, ex);
            return false;
        }
    }

    int getNumberFromDir(Path path) {
        return Integer.parseInt(path.getFileName().toString());
    }

}
