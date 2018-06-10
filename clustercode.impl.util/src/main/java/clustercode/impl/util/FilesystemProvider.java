package clustercode.impl.util;

import lombok.Synchronized;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class FilesystemProvider {

    private static FileSystem fs = FileSystems.getDefault();

    private FilesystemProvider() {

    }

    /**
     * Gets the file system.
     *
     * @return
     */
    @Synchronized
    public static FileSystem getInstance() {
        return fs;
    }

    /**
     * Sets the file system. This method is meant for testing and faking another file system.
     *
     * @param system
     */
    @Synchronized
    public static void setFileSystem(FileSystem system) {
        fs = system;
    }
}
