package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanSettings;
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

public class MarkSourceProcessorTest implements FileBasedUnitTest {

    private MarkSourceProcessor subject;
    private Path inputDir;

    @Mock
    private MediaScanSettings mediaScanSettings;
    @Spy
    private TranscodeResult transcodeResult;
    @Spy
    private CleanupContext context;
    @Spy
    private Media media;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        inputDir = getPath("input");
        context.setTranscodeResult(transcodeResult);
        transcodeResult.setMedia(media);
        transcodeResult.setSuccessful(true);
        when(mediaScanSettings.getSkipExtension()).thenReturn(".done");
        when(mediaScanSettings.getBaseInputDir()).thenReturn(inputDir);

        subject = new MarkSourceProcessor(mediaScanSettings);
    }

    @Test
    public void processStep_ShouldCreateFile_IfSourceDoesExist() throws Exception {
        Path source = createFile(getPath("0", "video.ext"));
        Path expected = inputDir.resolve("0").resolve("video.ext.done");

        createFile(inputDir.resolve(source));
        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldNotCreateFile_IfSourceDoesNotExist() throws Exception {
        Path source = getPath("0", "video.ext");
        Path expected = getPath("0", "video.ext.done");

        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).doesNotExist();
    }

}