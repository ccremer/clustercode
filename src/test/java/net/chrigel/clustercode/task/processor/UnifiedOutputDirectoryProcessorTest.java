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

public class UnifiedOutputDirectoryProcessorTest implements FileBasedUnitTest, ClockBasedUnitTest {

    private UnifiedOutputDirectoryProcessor subject;

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
        outputDir = getPath("output");
        context.setTranscodeResult(transcodeResult);
        transcodeResult.setMedia(media);
        when(settings.getOutputBaseDirectory()).thenReturn(outputDir);

        subject = new UnifiedOutputDirectoryProcessor(settings, getFixedClock(8, 20));
    }

    @Test
    public void processStep_ShouldMoveRootFileToOutputDir() throws Exception {

        Path temp = createFile(getPath("0", "video.ext"));

        transcodeResult.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.ext");

        assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

    @Test
    public void processStep_ShouldMoveSubdirFileToOutputDir() throws Exception {

        Path temp = createFile(getPath("0", "subdir", "video.ext"));

        transcodeResult.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.ext");

        assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

    @Test
    public void processStep_ShouldMoveSubdirFileToOutputDir_AndAddTimestampToFile_IfFileExists() throws Exception {
        Path temp = createFile(getPath("0", "subdir", "video.ext"));
        Path existing = createFile(outputDir.resolve("video.ext"));

        transcodeResult.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.2017-01-31.08-20-00.ext");

        assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

}