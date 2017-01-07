package net.chrigel.clustercode.util;

import lombok.Synchronized;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class FilesystemProvider {

    private static FileSystem fs = FileSystems.getDefault();

    private FilesystemProvider() {

    }

    @Synchronized
    public static FileSystem getInstance() {
        return fs;
    }

    @Synchronized
    public static void setFileSystem(FileSystem system) {
        fs = system;
    }
}
