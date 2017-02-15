package net.chrigel.clustercode;

import net.chrigel.clustercode.test.TestUtility;
import net.chrigel.clustercode.util.FileUtil;

import java.nio.file.Path;

public class ArbiterStartup {

    public static void main(String[] args) throws Exception {
        Path testDir = TestUtility.getTestDir();
        TestUtility.deleteFolderAndItsContent(testDir);
        FileUtil.createDirectoriesFor(testDir);

        Thread.sleep(500);
        Startup.main(new String[]{"ArbiterStartup.properties"});
    }
}
