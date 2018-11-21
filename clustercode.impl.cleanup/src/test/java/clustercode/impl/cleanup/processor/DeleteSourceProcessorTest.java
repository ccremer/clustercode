package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.domain.Media;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private CleanupConfig cleanupConfig;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;
    @Spy
    private CleanupContext context;
    @Spy
    private Media media;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        inputDir = getPath("input");
        when(cleanupConfig.base_input_dir()).thenReturn(inputDir);
        transcodeFinishedEvent.setMedia(media);
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        subject = new DeleteSourceProcessor(cleanupConfig);
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
