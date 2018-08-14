package clustercode.impl.scan;

import clustercode.api.scan.FileScanner;
import lombok.extern.slf4j.XSlf4j;
import org.slf4j.ext.XLogger;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XSlf4j
public class FileScannerImpl
    implements FileScanner {

    private boolean isDirEnabled;
    private Optional<Path> searchDir = Optional.empty();
    private Optional<List<String>> allowedExtensions = Optional.empty();
    private Optional<String> skipExtension = Optional.empty();
    private int depth;
    private Optional<Path> skipDirectory = Optional.empty();


    @Override
    public FileScanner searchIn(Path dir) {
        this.searchDir = Optional.of(dir);
        return this;
    }

    @Override
    public FileScanner withRecursion(boolean recursive) {
        if (recursive) {
            this.depth = Integer.MAX_VALUE;
        } else {
            this.depth = 1;
        }
        return this;
    }

    @Override
    public FileScanner withDepth(Integer value) {
        this.depth = value;
        return this;
    }

    @Override
    public FileScanner withDirectories(boolean dirs) {
        this.isDirEnabled = dirs;
        return this;
    }

    @Override
    public FileScanner withFileExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = Optional.of(allowedExtensions);
        return this;
    }

    @Override
    public FileScanner whileSkippingExtraFilesWith(String skipping) {
        this.skipExtension = Optional.of(skipping);
        return this;
    }

    @Override
    public FileScanner whileSkippingExtraFilesIn(Path dir) {
        this.skipDirectory = Optional.ofNullable(dir);
        return this;
    }

    @Override
    public Optional<List<Path>> scan() {
        try {
            return Optional.of(createStreamWithLogLevel(XLogger.Level.WARN).collect(Collectors.toList()));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Stream<Path> stream() {
        return createStreamWithLogLevel(XLogger.Level.ERROR);
    }

    @Override
    public Stream<Path> streamAndIgnoreErrors() {
        try {
            return createStreamWithLogLevel(XLogger.Level.WARN);
        } catch (RuntimeException e) {
            return Stream.empty();
        }
    }

    private Stream<Path> createStreamWithLogLevel(XLogger.Level logLevel) {
        try {
            return Files
                .walk(searchDir.get(), this.depth, FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !path.equals(searchDir.get()))
                .filter(this::includeFileOrDirectory)
                .filter(this::hasAllowedExtension)
                .filter(this::hasNotCompanionFile);
        } catch (IOException e) {
           // log.catching(logLevel, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests whether the given sourcePath name ends with an extension specified with {@link #withFileExtensions(List)}.
     * Returns true if no matcher is present.
     *
     * @param path
     * @return true if no matcher present or at least one of the extensions is applicable, otherwise false.
     */
    boolean hasAllowedExtension(Path path) {
        return allowedExtensions.map(strings -> strings
            .stream()
            .anyMatch(extension -> path.getFileName().toString().endsWith(extension))
        ).orElse(true);
    }

    /**
     * Tests whether the given file has not another file named with the {@link #whileSkippingExtraFilesWith(String)}
     * extension. E.g. if both files "foo/bar" and "foo/bar.skipping" exist, then this method returns false,
     * otherwise true.
     *
     * @param path the base file name.
     * @return false if there is a companion file, true if the companion file does not exist or the extension is not
     * specified.
     */
    boolean hasNotCompanionFile(Path path) {
        if (skipExtension.isPresent()) {
            Path sibling = path.resolveSibling(path.getFileName() + skipExtension.get());
            boolean companionFileExists = Files.exists(sibling);
            boolean markDirFileExists = skipDirectory.map(dir -> {
                Path siblingInDir = searchDir.get().getParent().relativize(sibling);
                Path toChck = dir.resolve(siblingInDir);
                return Files.exists(toChck);
            }).orElse(false);
            if (companionFileExists || markDirFileExists) log.debug("Ignoring: {}", path);
            return !(companionFileExists || markDirFileExists);
        } else {
            return true;
        }
    }

    /**
     * Tests whether the sourcePath is being included by determining {@link #withDirectories(boolean)}. If the
     * directories flag is enabled, this method returns whether {@code sourcePath} is a directory, otherwise it tests if
     * {@code sourcePath} is a regular file.
     *
     * @param path the sourcePath.
     * @return true if the dir flag is enabled and sourcePath is a dir, true if dir flag is disabled and sourcePath
     * is a file, false otherwise.
     */
    boolean includeFileOrDirectory(Path path) {
        if (isDirEnabled) {
            return Files.isDirectory(path);
        } else {
            return Files.isRegularFile(path);
        }
    }

}
