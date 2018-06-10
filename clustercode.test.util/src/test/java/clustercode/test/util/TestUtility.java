package clustercode.test.util;

import clustercode.impl.util.FileUtil;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class TestUtility {

    public static Path getTestResourcesDir() {
        return Paths.get("build", "resources", "test");
    }

    public static Path getIntegrationTestResourcesDir() {
        return Paths.get("build", "resources", "integrationTest");
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
