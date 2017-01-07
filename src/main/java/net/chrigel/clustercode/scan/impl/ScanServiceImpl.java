package net.chrigel.clustercode.scan.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.FileScanner;
import net.chrigel.clustercode.scan.ScanService;
import net.chrigel.clustercode.scan.ScanSettings;
import net.chrigel.clustercode.task.MediaCandidate;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XSlf4j
public class ScanServiceImpl implements ScanService {

    private final ScanSettings scanSettings;
    private final Provider<FileScanner> scannerProvider;

    @Inject
    ScanServiceImpl(ScanSettings scanSettings, Provider<FileScanner> scannerProvider) {
        this.scanSettings = scanSettings;
        this.scannerProvider = scannerProvider;
    }

    @Override
    public Map<Path, List<MediaCandidate>> retrieveFiles() {
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

    List<MediaCandidate> getListOfMediaFiles(Path path) {
        return scannerProvider.get()
                .searchIn(path)
                .withRecursion(true)
                .withFileExtensions(scanSettings.getAllowedExtensions())
                .whileSkippingExtraFilesWith(scanSettings.getSkipExtension())
                .streamAndIgnoreErrors()
                .map(file -> MediaCandidate.builder()
                        .sourcePath(scanSettings.getBaseInputDir().relativize(file))
                        .priority(getNumberFromDir(path))
                        .build()
                )
                .peek(candidate -> log.info("Found file: Priority: {}, sourcePath: {}",
                        candidate.getPriority(), candidate.getSourcePath()))
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
