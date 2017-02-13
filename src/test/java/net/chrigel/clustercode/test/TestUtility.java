package net.chrigel.clustercode.test;

import net.chrigel.clustercode.util.FileUtil;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

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

    public static void createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFolderAndItsContent(final Path folder) throws IOException {
        if (Files.exists(folder)) {
            Files.walk(folder)
                    .sorted(Comparator.reverseOrder())
                    .forEach(FileUtil::deleteFile);
        }
    }
}
