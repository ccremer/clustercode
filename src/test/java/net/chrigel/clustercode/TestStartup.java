package net.chrigel.clustercode;

import net.chrigel.clustercode.test.TestUtility;
import net.chrigel.clustercode.util.FileUtil;

import java.nio.file.*;
import java.util.Arrays;

public class TestStartup {

    public static void main(String[] args) throws Exception {
        Path testDir = TestUtility.getTestDir();
        TestUtility.deleteFolderAndItsContent(testDir);
        FileUtil.createDirectoriesFor(testDir);
        Path inputDir = testDir.resolve("input").resolve("0");
        Path profileDir = testDir.resolve("profiles").resolve("0");
        Path tempDir = testDir.resolve("tmp");
        FileUtil.createDirectoriesFor(profileDir);
        FileUtil.createDirectoriesFor(inputDir);
        FileUtil.createDirectoriesFor(tempDir);
        FileUtil.createDirectoriesFor(testDir.resolve("output"));

        TestUtility.createFile(tempDir.resolve("video.mp4"));
        TestUtility.createFile(inputDir.resolve("video.mkv"));
        Files.write(profileDir.resolve("profile.handbrake"), Arrays.asList("--input ${INPUT}", "--output ${OUTPUT}"));

        Thread.sleep(500);
        Startup.main(new String[]{"TestStartup.properties"});
    }

}
