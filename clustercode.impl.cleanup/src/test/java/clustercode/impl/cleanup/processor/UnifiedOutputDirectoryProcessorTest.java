package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.domain.Media;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.test.util.ClockBasedUnitTest;
import clustercode.test.util.FileBasedUnitTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;

import static org.mockito.Mockito.when;

public class UnifiedOutputDirectoryProcessorTest
        implements FileBasedUnitTest, ClockBasedUnitTest {

    private UnifiedOutputDirectoryProcessor subject;

    @Mock
    private CleanupConfig cleanupConfig;
    @Spy
    private CleanupContext context;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;
    @Spy
    private Media media;

    private Path outputDir;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        outputDir = getPath("output");
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        transcodeFinishedEvent.setMedia(media);
        when(cleanupConfig.base_output_dir()).thenReturn(outputDir);
        when(transcodeFinishedEvent.isSuccessful()).thenReturn(true);

        subject = new UnifiedOutputDirectoryProcessor(cleanupConfig, getFixedClock(8, 20));
    }

    @Test
    public void processStep_ShouldMoveRootFileToOutputDir() throws Exception {

        Path temp = createFile(getPath("0", "video.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.ext");

        Assertions.assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

    @Test
    public void processStep_ShouldMoveSubdirFileToOutputDir() throws Exception {

        Path temp = createFile(getPath("0", "subdir", "video.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.ext");

        Assertions.assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

    @Test
    public void processStep_ShouldMoveSubdirFileToOutputDir_AndAddTimestampToFile_IfFileExists() throws Exception {
        Path temp = createFile(getPath("0", "subdir", "video.ext"));
        createFile(outputDir.resolve("video.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.2017-01-31.08-20-00.ext");

        Assertions.assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

}
