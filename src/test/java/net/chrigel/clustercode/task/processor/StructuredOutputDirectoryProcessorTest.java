package net.chrigel.clustercode.task.processor;

import net.chrigel.clustercode.task.CleanupContext;
import net.chrigel.clustercode.task.CleanupSettings;
import net.chrigel.clustercode.task.Media;
import net.chrigel.clustercode.test.ClockBasedUnitTest;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import net.chrigel.clustercode.transcode.TranscodeResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class StructuredOutputDirectoryProcessorTest
        implements FileBasedUnitTest, ClockBasedUnitTest {

    private StructuredOutputDirectoryProcessor subject;

    @Mock
    private CleanupSettings settings;
    @Spy
    private CleanupContext context;
    @Spy
    private TranscodeResult transcodeResult;
    @Spy
    private Media media;

    private Path outputDir;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        context.setTranscodeResult(transcodeResult);
        transcodeResult.setMedia(media);

        outputDir = getPath("output");
        when(settings.getOutputBaseDirectory()).thenReturn(outputDir);

        subject = new StructuredOutputDirectoryProcessor(settings, getFixedClock(8, 20));
    }

    @Test
    public void processStep_ShouldMoveFileToNewDestination() throws Exception {

        Path source = createFile(getPath("0", "subdir", "file.ext"));
        Path temp = createFile(getPath("tmp", "file.ext"));

        transcodeResult.setTemporaryPath(temp);
        media.setSourcePath(source);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "subdir", "file.ext");
        assertThat(result.getOutputPath()).isEqualTo(expected);
        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldMoveFileWithTimestamp_IfFileExists() throws Exception {

        Path source = createFile(getPath("0", "subdir", "file.ext"));
        Path existing = createFile(getPath("output", "subdir", "file.ext"));
        Path temp = createFile(getPath("tmp", "file.ext"));

        transcodeResult.setTemporaryPath(temp);
        media.setSourcePath(source);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "subdir", "file.2017-01-31.08-20-00.ext");
        assertThat(result.getOutputPath()).isEqualTo(expected);
        assertThat(expected).exists();
    }

    @Test
    public void createOutputDirectoryTree_ShouldRecreateDirectoryTree_WithSubdirectories() throws Exception {
        Path source = getPath("0", "subdir1", "subdir2", "file.ext");
        Path expected = outputDir.resolve("subdir1").resolve("subdir2").resolve("file.ext");

        Path result = subject.createOutputDirectoryTree(source);

        assertThat(result)
                .isEqualTo(expected)
                .hasParentRaw(expected.getParent());
    }

    @Test
    public void createOutputDirectoryTree_ShouldRecreateDirectoryTree_WithoutSubdirs() throws Exception {
        Path source = getPath("0", "file.ext");
        Path expected = outputDir.resolve("file.ext");

        Path result = subject.createOutputDirectoryTree(source);

        assertThat(result)
                .isEqualTo(expected);
    }

}