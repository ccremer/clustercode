package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import net.chrigel.clustercode.util.InvalidConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

public class FileSizeConstraintTest implements FileBasedUnitTest {

    private FileSizeConstraint subject;
    private Path inputDir;

    @Mock
    private MediaScanSettings scanSettings;

    @Spy
    private Media media;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        when(scanSettings.getBaseInputDir()).thenReturn(getPath("input"));
        inputDir = scanSettings.getBaseInputDir();
        Files.createDirectory(inputDir);
    }

    private void initSubject(long minSize, long maxSize) {
        subject = new FileSizeConstraint(minSize, maxSize, FileSizeConstraint.BYTES, scanSettings);
    }

    private void writeBytes(int count, Path path) throws IOException {
        Files.write(path, new byte[count]);
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize() throws Exception {
        long minSize = 10;
        long maxSize = 1024;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize_AndMaxSizeDisabled() throws Exception {
        long minSize = 10;
        long maxSize = 0;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsSmallerThanMinSize_AndMMinSizeDisabled() throws Exception {
        long minSize = 0;
        long maxSize = 16;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileIsSmallerThanMinSize() throws Exception {
        long minSize = 10;
        long maxSize = 1024;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(8, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileIsGreaterThanMaxSize() throws Exception {
        long minSize = 1000000;
        long maxSize = 10000000;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(101, file);

        media.setSourcePath(inputDir.relativize(file));
        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileSizeCannotBeDetermined() throws Exception {
        long minSize = 10;
        long maxSize = 100;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");

        media.setSourcePath(inputDir.relativize(file));
        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    public void ctor_ShouldThrowException_IfFileSizesEqual() throws Exception {
        long minSize = 0;
        long maxSize = 0;
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() -> initSubject(minSize, maxSize));
    }

    @Test
    public void ctor_ShouldThrowException_IfConfiguredIncorrectly_WhenSizesSwapped() throws Exception {
        long minSize = 12;
        long maxSize = 1;
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() -> initSubject(minSize, maxSize));
    }

    @Test
    public void ctor_ShouldThrowException_IfConfiguredIncorrectly_WhenSizesNegative() throws Exception {
        long minSize = -1;
        long maxSize = 1;
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() -> initSubject(minSize, maxSize));
    }

}