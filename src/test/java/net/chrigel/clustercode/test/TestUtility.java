package net.chrigel.clustercode.test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtility {

    public static Path getTestDir() {
        return Paths.get("target", "test-resources");
    }

    public static Path getTestDir(FileSystem fs) {
        return fs.getPath("target", "test-resources");
    }

    public static Path getTestResourcesDir() {
        return Paths.get("src", "test", "resources");
    }

    public static Path getTestResourcesDir(FileSystem fs) {
        return fs.getPath("src", "test", "resources");
    }

}
