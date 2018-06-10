package clustercode.impl.util;

import lombok.extern.slf4j.XSlf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@XSlf4j
public class FileUtil {

    /**
     * Gets the file name of the given path without the extension. The extension begins at the last occurrence of ".".
     * Returns the base name if there is no extension or the file begins with a dot (".").
     *
     * @param path the path.
     * @return the base file name (without directory path and leading ".").
     * @throws NullPointerException if the path has zero elements or is null.
     * @see Path#getFileName()
     */
    public static String getFileNameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        if (index <= 0) {
            return name;
        } else {
            return name.substring(0, index);
        }
    }

    /**
     * Gets the file extension of the given path. The extension begins at the last occurrence of ".". Returns an empty
     * string if the file does not contain a dot (".") or begins with one (hidden file in unix).
     *
     * @param path the path.
     * @return the extension, with leading dot (".").
     * @throws NullPointerException if path is null or has zero elements.
     * @see Path#getFileName()
     */
    public static String getFileExtension(Path path) {
        String ext = path.getFileName().toString();
        int index = ext.lastIndexOf('.');
        if (index <= 0) {
            return "";
        } else {
            return ext.substring(index, ext.length());
        }
    }

    /**
     * Creates a timestamped path from the given base file. Example: "/path/basefile.ext" becomes
     * "/path/basefile.2017-01-23.14-34-20.ext" if the formatter is of pattern "yyyy-MM-dd.HH-mm-ss".
     *
     * @param baseFile  the base name.
     * @param time      the timestamp to format.
     * @param formatter the formatter for the timestamp.
     * @return a new path which has a timestamp in its filename.
     * @throws java.time.DateTimeException if the time cannot be formatted.
     */
    public static Path getTimestampedPath(Path baseFile, TemporalAccessor time, DateTimeFormatter formatter) {
        return getTimestampedPath(baseFile, formatter.format(time));
    }

    /**
     * Creates a timestamped path from the given base file. Example: "/path/basefile.ext" becomes
     * "/path/basefile.2017-01-23.14-34-20.ext" if the given timestamp equals to "2017-01-23.14-34-20".
     *
     * @param baseFile           the base name.
     * @param formattedTimestamp the pre-formatted timestamp. A leading dot will be added automatically.
     * @return a new path which has a timestamp in its filename.
     * @throws java.nio.file.InvalidPathException if the path cannot be constructed from the given string.
     */
    public static Path getTimestampedPath(Path baseFile, String formattedTimestamp) {
        String ext = getFileExtension(baseFile);
        String timestamp = ".".concat(formattedTimestamp);
        return baseFile.resolveSibling(getFileNameWithoutExtension(baseFile)
                .concat(timestamp).concat(ext));
    }

    /**
     * Creates the parent directories for the given path.
     *
     * @param target the target path. Must not be a root path.
     * @throws RuntimeException if the dirs could not be created.
     * @see Path#getParent()
     */
    public static void createParentDirectoriesFor(Path target) {
        createDirectoriesFor(target.getParent());
    }

    /**
     * Creates the parent directories for the given path.
     *
     * @param target the target path.
     * @throws RuntimeException if the dirs could not be created.
     * @see Files#createDirectories(Path, FileAttribute[])
     */
    public static void createDirectoriesFor(Path target) {
        try {
            log.debug("Creating directories for {}", target);
            Files.createDirectories(target);
        } catch (IOException e) {
            log.error("Could not create directories for {}: {}", target, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the given file. If an IOException occurs, a warning will be logged.
     *
     * @param file
     */
    public static void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            log.warn("Could not delete file:", e);
        }
    }
}
