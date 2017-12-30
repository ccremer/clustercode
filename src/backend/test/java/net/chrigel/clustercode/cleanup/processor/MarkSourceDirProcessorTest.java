package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MarkSourceDirProcessorTest implements FileBasedUnitTest {

    private MarkSourceDirProcessor subject;
    private Path inputDir;
    private Path markDir;

    @Mock
    private CleanupSettings cleanupSettings;
    @Mock
    private MediaScanSettings mediaScanSettings;
    @Spy
    private CleanupContext context;
    @Spy
    private Media media;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        inputDir = getPath("input");
        markDir = getPath("mark");
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        transcodeFinishedEvent.setMedia(media);
        transcodeFinishedEvent.setSuccessful(true);
        when(mediaScanSettings.getSkipExtension()).thenReturn(".done");
        when(mediaScanSettings.getBaseInputDir()).thenReturn(inputDir);
        when(cleanupSettings.getMarkSourceDirectory()).thenReturn(Optional.of(markDir));
        subject = new MarkSourceDirProcessor(mediaScanSettings, cleanupSettings);
    }

    @Test
    public void processStep_ShouldRecreateDirectoryStructure() throws Exception {
        Path source = createFile(getPath("0", "video.ext"));
        Path expected = markDir.resolve("0").resolve("video.ext.done");

        createFile(inputDir.resolve(source));
        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldRecreateDirectoryStructure_WithSubdirectories() throws Exception {
        Path source = createFile(getPath("0","movies", "video.ext"));
        Path expected = markDir.resolve("0").resolve("movies").resolve("video.ext.done");

        createFile(inputDir.resolve(source));
        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldNotCreateDirectoryStructure_IfSourceDoesNotExist() throws Exception {
        Path source = getPath("0","movies", "video.ext");
        Path expected = markDir.resolve("0").resolve("movies").resolve("video.ext.done");

        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).doesNotExist();
    }
}
