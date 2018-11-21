package clustercode.impl.transcode;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.process.ExternalProcessService;
import clustercode.api.process.ProcessConfiguration;
import clustercode.api.process.RunningExternalProcess;
import clustercode.test.util.CompletableUnitTest;
import clustercode.test.util.FileBasedUnitTest;
import io.reactivex.Single;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TranscodingServiceImplTest implements FileBasedUnitTest, CompletableUnitTest {

    @Mock
    private ExternalProcessService process;
    @Mock
    private RunningExternalProcess runningProcessMock;
    @Mock
    private TranscoderConfig transcoderConfig;

    @Spy
    private Media media;
    @Spy
    private Profile profile;
    @Spy
    private TranscodeTask task;

    private TranscodingServiceImpl subject;

    private ProcessConfiguration processConfiguration;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        processConfiguration = ProcessConfiguration.builder()
                                                   .executable(getPath("mock"))
                                                   .build();

        when(media.getSourcePath()).thenReturn(getPath("0", "video.mkv"));
        when(profile.getFields()).thenReturn(Collections.singletonMap("FORMAT", ".mp4"));
        when(transcoderConfig.temporary_dir()).thenReturn(getPath("tmp"));
        when(transcoderConfig.base_input_dir()).thenReturn(getPath("root"));
        when(profile.getArguments()).thenReturn(Collections.emptyList());

        task.setMedia(media);
        task.setProfile(profile);

        subject = new TranscodingServiceImpl(
            transcoderConfig
        );
    }

    @Test
    public void replaceOutput_ShouldReplaceOutput_WithNewValue() throws Exception {

        Path output = getPath("tmp", "video.mp4");
        when(profile.getArguments()).thenReturn(Collections.singletonList(TranscodingServiceImpl.OUTPUT_PLACEHOLDER));

        String result = subject.replaceOutput(
            TranscodingServiceImpl.OUTPUT_PLACEHOLDER,
            output
        );

        assertThat(result).isEqualTo(output.toString());
    }

    @Test
    public void replaceInput_ShouldReplaceInput_WithBasePath() throws Exception {
        Path input = getPath("0", "video.mkv");
        when(profile.getArguments()).thenReturn(Arrays.asList());

        String result = subject.replaceInput(
            TranscodingServiceImpl.INPUT_PLACEHOLDER,
            input);

        assertThat(result).isEqualTo(transcoderConfig.base_input_dir().resolve(input).toString());
    }

    @Test
    public void transcode_ShouldFireEvent_IfTranscodingFailed() {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            when(process.start(any(), any())).thenReturn(Single.just(1));

            subject.onTranscodeFinished()
                   .subscribe(result -> {
                       assertThat(result.isSuccessful()).isFalse();
                       completeOne();
                   });
            subject.transcode(task);

            waitForCompletion();
        });
    }

    @Test
    public void transcode_ShouldFireEvent_IfTranscodingFailed_OnException() {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            when(process.start(any(), any())).thenReturn(Single.error(IOException::new));

            subject.onTranscodeFinished()
                   .subscribe(result -> {
                       assertThat(result.isSuccessful()).isFalse();
                       completeOne();
                   });
            subject.transcode(task);

            waitForCompletion();
        });
    }

    @Test
    public void transcode_ShouldFireEvent_IfTranscodingBegins() {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            when(process.start(any(), any())).thenReturn(Single.just(0));

            subject.onTranscodeBegin()
                   .subscribe(result -> {
                       assertThat(result.getTask()).isEqualTo(task);
                       completeOne();
                   });
            subject.transcode(task);

            waitForCompletion();
        });
    }
}
