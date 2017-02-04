package net.chrigel.clustercode.util;

import net.chrigel.clustercode.test.ClockBasedUnitTest;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class FileUtilTest implements FileBasedUnitTest, ClockBasedUnitTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm-ss");

    @Before
    public void setUp() throws Exception {
        setupFileSystem();
    }

    @Test
    public void getFileNameWithoutExtension_ShouldReturnFileName() throws Exception {

        Path path = getPath("0", "file.ext");
        String result = FileUtil.getFileNameWithoutExtension(path);

        assertThat(result).isEqualTo("file");
    }

    @Test
    public void getFileNameWithoutExtension_ShouldReturnFileName_IfNoExtensionPresent() throws Exception {

        Path path = getPath("0", "file");
        String result = FileUtil.getFileNameWithoutExtension(path);

        assertThat(result).isEqualTo("file");
    }

    @Test
    public void getFileNameWithoutExtension_ShouldReturnFileName_IfFileBeginsWithDot() throws Exception {

        Path path = getPath("0", ".file");
        String result = FileUtil.getFileNameWithoutExtension(path);

        assertThat(result).isEqualTo(".file");
    }

    @Test
    public void getFileExtension_ShouldReturnExtension() throws Exception {

        Path path = getPath("file.ext");
        String result = FileUtil.getFileExtension(path);

        assertThat(result).isEqualTo(".ext");
    }

    @Test
    public void getFileExtension_ShouldReturnEmptyString_IfNoExtensionPresent() throws Exception {

        Path path = getPath("file");
        String result = FileUtil.getFileExtension(path);

        assertThat(result).isEmpty();
    }

    @Test
    public void getFileExtension_ShouldReturnEmptyString_IfFileBeginsWithDot() throws Exception {

        Path path = getPath("dir", ".file");
        String result = FileUtil.getFileExtension(path);

        assertThat(result).isEmpty();
    }

    @Test
    public void getTimestampedPath_ShouldReturnTimestampedPath_WhenUsingFormatter() throws Exception {

        Path path = getPath("root", "file.ext");
        Path result = FileUtil.getTimestampedPath(path, getLocalTime(12, 31), formatter);

        assertThat(result).hasFileName("file.2017-01-31.12-31-00.ext");
        assertThat(result).hasParentRaw(path.getParent());
    }

    @Test
    public void getTimestampedPath_ShouldReturnTimestampedPath_WhenUsingString() throws Exception {

        Path path = getPath("root", "file.ext");
        Path result = FileUtil.getTimestampedPath(path, "2017-01-31.12-31-54");

        assertThat(result).hasFileName("file.2017-01-31.12-31-54.ext");
        assertThat(result).hasParentRaw(path.getParent());
    }
}