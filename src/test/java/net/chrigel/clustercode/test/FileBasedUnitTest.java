package net.chrigel.clustercode.test;

import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a test utility where unit testing using files is needed. Any path created using {@link #getPath(String,
 * String...)} is placed in-memory. The default backend of the file system is Google's Jimfs.
 */
public interface FileBasedUnitTest {

    /**
     * The variable used by {@link FileBasedUnitTest} to access the (existing) file system.
     */
    AtomicReference<FileSystem> _fs = new AtomicReference<>();

    /**
     * Setup the file system. This method initializes {@link #_fs} with a FileSystem, by default this is Jimfs.
     */
    default void setupFileSystem() {
        _fs.set(Jimfs.newFileSystem());
    }

    /**
     * Gets the path according to {@link FileSystem#getPath(String, String...)}. Be sure to call {@link
     * #setupFileSystem()} first (e.g. in your @Before method).
     *
     * @param first
     * @param more
     * @return the path.
     * @throws RuntimeException if {@link #_fs} is not initialized.
     */
    default Path getPath(String first, String... more) {
        if (_fs.get() == null) {
            throw new RuntimeException(
                    "File system is not initialized. Call setupFileSystem() in your @Before method.");
        }
        return _fs.get().getPath(first, more);
    }

    /**
     * Creates the file and returns the path. By default any parent directories will be created first.
     *
     * @param path the desired location of the file.
     * @return path
     * @throws RuntimeException with the original IOException as cause if it failed.
     */
    default Path createFile(Path path) {
        try {
            Files.createFile(createParentDirOf(path));
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recursively creates the directories of the given path.
     * @param path
     * @return path
     * @throws RuntimeException with the original IOException as cause if it failed.
     */
    default Path createDirectory(Path path) {
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the parent directory of the given path.
     *
     * @param path
     * @return path
     * @throws RuntimeException with the original IOException as cause if it failed.
     */
    default Path createParentDirOf(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
