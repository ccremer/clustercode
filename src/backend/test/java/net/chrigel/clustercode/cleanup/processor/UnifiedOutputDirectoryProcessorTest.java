package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.test.ClockBasedUnitTest;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UnifiedOutputDirectoryProcessorTest
        implements FileBasedUnitTest, ClockBasedUnitTest {

    private UnifiedOutputDirectoryProcessor subject;

    @Mock
    private CleanupSettings settings;
    @Spy
    private CleanupContext context;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;
    @Spy
    private Media media;

    private Path outputDir;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        outputDir = getPath("output");
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        transcodeFinishedEvent.setMedia(media);
        when(settings.getOutputBaseDirectory()).thenReturn(outputDir);
        when(transcodeFinishedEvent.isSuccessful()).thenReturn(true);

        subject = new UnifiedOutputDirectoryProcessor(settings, getFixedClock(8, 20));
    }

    @Test
    public void processStep_ShouldMoveRootFileToOutputDir() throws Exception {

        Path temp = createFile(getPath("0", "video.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.ext");

        assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

    @Test
    public void processStep_ShouldMoveSubdirFileToOutputDir() throws Exception {

        Path temp = createFile(getPath("0", "subdir", "video.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        CleanupContext result = subject.processStep(context);

        Path expected = getPath("output", "video.ext");

        assertThat(result.getOutputPath())
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

        assertThat(result.getOutputPath())
                .isEqualTo(expected)
                .exists();
    }

}
