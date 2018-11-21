package clustercode.impl.constraint;

import clustercode.api.domain.Media;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private ConstraintConfig config;

    @Spy
    private Media media;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        when(config.base_input_dir()).thenReturn(getPath("input"));
        inputDir = config.base_input_dir();
        Files.createDirectory(inputDir);
    }

    private void initSubject() {
        subject = new FileSizeConstraint(config, FileSizeConstraint.BYTES);
    }

    private void writeBytes(int count, Path path) throws IOException {
        Files.write(path, new byte[count]);
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize() throws Exception {
        when(config.max_file_size()).thenReturn(1024L);
        when(config.min_file_size()).thenReturn(10L);
        initSubject();

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize_AndMaxSizeDisabled() throws Exception {
        when(config.max_file_size()).thenReturn(104L);
        when(config.min_file_size()).thenReturn(0L);
        initSubject();

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    public void accept_ShouldReturnTrue_IfFileIsSmallerThanMinSize_AndMinSizeDisabled() throws Exception {
        when(config.max_file_size()).thenReturn(16L);
        when(config.min_file_size()).thenReturn(0L);
        initSubject();

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(12, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileIsSmallerThanMinSize() throws Exception {
        when(config.max_file_size()).thenReturn(1024L);
        when(config.min_file_size()).thenReturn(10L);
        initSubject();

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(8, file);

        media.setSourcePath(inputDir.relativize(file));

        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileIsGreaterThanMaxSize() throws Exception {
        when(config.max_file_size()).thenReturn(10000000L);
        when(config.min_file_size()).thenReturn(1000000L);
        initSubject();

        Path file = inputDir.resolve("movie.mp4");
        writeBytes(101, file);

        media.setSourcePath(inputDir.relativize(file));
        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfFileSizeCannotBeDetermined() throws Exception {
        when(config.max_file_size()).thenReturn(100L);
        when(config.min_file_size()).thenReturn(10L);
        initSubject();

        Path file = inputDir.resolve("movie.mp4");

        media.setSourcePath(inputDir.relativize(file));
        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    public void ctor_ShouldThrowException_IfFileSizesEqual() throws Exception {
        when(config.max_file_size()).thenReturn(0L);
        when(config.min_file_size()).thenReturn(0L);
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(this::initSubject);
    }

    @Test
    public void ctor_ShouldThrowException_IfConfiguredIncorrectly_WhenSizesSwapped() throws Exception {
        when(config.max_file_size()).thenReturn(1L);
        when(config.min_file_size()).thenReturn(12L);
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(this::initSubject);
    }

    @Test
    public void ctor_ShouldThrowException_IfConfiguredIncorrectly_WhenSizesNegative() throws Exception {
        when(config.max_file_size()).thenReturn(-1L);
        when(config.min_file_size()).thenReturn(-1L);
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(this::initSubject);
    }

}
