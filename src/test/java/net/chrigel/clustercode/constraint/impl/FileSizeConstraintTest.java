package net.chrigel.clustercode.constraint.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.chrigel.clustercode.scan.ScanSettings;
import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.util.FilesystemProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class FileSizeConstraintTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private FileSizeConstraint subject;
    private Path inputDir;
    private FileSystem fs;

    @Mock
    private ScanSettings scanSettings;

    @Spy
    private MediaCandidate media;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        FilesystemProvider.setFileSystem(Jimfs.newFileSystem(Configuration.forCurrentPlatform()));
        fs = FilesystemProvider.getInstance();
        when(scanSettings.getBaseInputDir()).thenReturn(FilesystemProvider.getInstance().getPath("input"));
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

        assertThat(subject.accept(media), equalTo(true));
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize_AndMaxSizeDisabled() throws Exception {
        long minSize = 10;
        long maxSize = 0;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media), equalTo(true));
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsSmallerThanMinSize_AndMMinSizeDisabled() throws Exception {
        long minSize = -1;
        long maxSize = 16;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media), equalTo(true));
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileIsSmallerThanMinSize() throws Exception {
        long minSize = 10;
        long maxSize = 1024;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(8, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media), equalTo(false));
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileIsGreaterThanMaxSize() throws Exception {
        long minSize = 10;
        long maxSize = 100;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(101, file);

        media.setSourcePath(inputDir.relativize(file));
        assertThat(subject.accept(media), equalTo(false));
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileSizeCannotBeDetermined() throws Exception {
        long minSize = 10;
        long maxSize = 100;
        initSubject(minSize, maxSize);

        Path file = inputDir.resolve("movie.mp4");

        media.setSourcePath(inputDir.relativize(file));
        assertThat(subject.accept(media), equalTo(false));
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileSizeConstraintIsDisabled() throws Exception {
        long minSize = 0;
        long maxSize = 0;
        initSubject(minSize, maxSize);

        assertThat(subject.accept(media), equalTo(true));
    }

    @Test
    public void constructor_ShouldThrowException_IfConfiguredIncorrectly() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        long minSize = 12;
        long maxSize = 1;
        initSubject(minSize, maxSize);
    }

}