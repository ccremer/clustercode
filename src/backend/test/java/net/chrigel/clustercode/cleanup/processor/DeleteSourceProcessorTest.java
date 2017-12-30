package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeleteSourceProcessorTest implements FileBasedUnitTest {

    private DeleteSourceProcessor subject;
    private Path inputDir;

    @Mock
    private MediaScanSettings mediaScanSettings;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;
    @Spy
    private CleanupContext context;
    @Spy
    private Media media;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        inputDir = getPath("input");
        when(mediaScanSettings.getBaseInputDir()).thenReturn(inputDir);
        transcodeFinishedEvent.setMedia(media);
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        subject = new DeleteSourceProcessor(mediaScanSettings);
    }

    @Test
    public void processStep_ShouldDeleteSourceFile_IfFileExists() throws Exception {

        Path source = createFile(getPath("0", "video.ext"));
        media.setSourcePath(source);

        subject.processStep(context);

        assertThat(inputDir.resolve(source)).doesNotExist();
    }

    @Test
    public void processStep_ShouldDoNothing_IfFileNotExists() throws Exception {
        Path source = getPath("0", "video.ext");
        media.setSourcePath(source);

        subject.processStep(context);

        assertThat(source).doesNotExist();
    }
}
