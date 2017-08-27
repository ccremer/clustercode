package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import net.chrigel.clustercode.transcode.TranscodeResult;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TranscodingServiceImplTest implements FileBasedUnitTest {

    @Mock
    private ExternalProcess process;
    @Mock
    private TranscoderSettings transcoderSettings;
    @Mock
    private MediaScanSettings mediaScanSettings;
    @Mock
    private OutputParser<FfmpegOutput> ffmpegParser;
    @Mock
    private OutputParser<FfprobeOutput> ffprobeParser;

    @Spy
    private ProgressCalculator progressCalculator;

    @Spy
    private Media media;
    @Spy
    private Profile profile;
    @Spy
    private TranscodeTask task;

    private TranscodingServiceImpl subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        when(process.withExecutablePath(any())).thenReturn(process);
        when(process.withIORedirected(anyBoolean())).thenReturn(process);
        when(process.withArguments(any())).thenReturn(process);
        when(process.withStderrParser(any())).thenReturn(process);
        when(process.withStdoutParser(any())).thenReturn(process);
        when(media.getSourcePath()).thenReturn(getPath("0", "video.mkv"));
        when(profile.getFields()).thenReturn(Collections.singletonMap("FORMAT", ".mp4"));
        when(transcoderSettings.getTemporaryDir()).thenReturn(getPath("tmp"));
        when(mediaScanSettings.getBaseInputDir()).thenReturn(getPath("/root"));
        when(profile.getArguments()).thenReturn(Collections.emptyList());
        when(ffprobeParser.getResult()).thenReturn(Optional.empty());

        task.setMedia(media);
        task.setProfile(profile);

        subject = new TranscodingServiceImpl(() -> process,
                transcoderSettings,
                mediaScanSettings,
                ffmpegParser,
                ffprobeParser,
                progressCalculator);
    }

    @Test
    public void doTranscode_ShouldReplaceOutput_WithNewValue() throws Exception {
        Path output = getPath("tmp", "video.mp4");
        when(profile.getArguments()).thenReturn(Arrays.asList(TranscodingServiceImpl.OUTPUT_PLACEHOLDER));
        when(process.start()).thenReturn(Optional.of(0));

        Optional<Integer> result = subject.doTranscode(
                media.getSourcePath(), output, profile);

        assertThat(result).hasValue(0);
        verify(process).withArguments(Arrays.asList(output.toString()));
    }

    @Test
    public void doTranscode_ShouldReplaceInput_WithNewValue() throws Exception {
        Path input = getPath("0", "video.mkv");
        when(profile.getArguments()).thenReturn(Arrays.asList(TranscodingServiceImpl.INPUT_PLACEHOLDER));
        when(process.start()).thenReturn(Optional.of(0));

        Optional<Integer> result = subject.doTranscode(
                media.getSourcePath(), input, profile);

        assertThat(result).hasValue(0);
        verify(process).withArguments(Arrays.asList(mediaScanSettings.getBaseInputDir().resolve(input).toString()));
    }

    @Test
    public void transcode_ShouldReturnTrue_IfTranscodingSuccessful() throws Exception {
        when(process.start()).thenReturn(Optional.of(0));

        TranscodeResult result = subject.transcode(task);
        assertThat(result.getTemporaryPath()).isEqualTo(getPath("tmp", "video.mp4"));
        assertThat(result.isSuccessful()).isTrue();
    }

    @Test(timeout = 1000)
    public void transcode_ShouldRunAsync() throws Exception {
        when(process.start()).thenReturn(Optional.of(0));

        Semaphore blocker = new Semaphore(0);
        subject.transcode(task, result -> {
            assertThat(result.isSuccessful()).isTrue();
            blocker.release();
        });

        blocker.acquire();
    }

    @Test
    public void transcode_ShouldReturnFalse_IfTranscodingFailed() throws Exception {
        when(process.start()).thenReturn(Optional.of(1));

        TranscodeResult result = subject.transcode(task);
        assertThat(result.isSuccessful()).isFalse();
    }

    @Test
    public void transcode_ShouldReturnFalse_IfTranscodingUndetermined() throws Exception {
        when(process.start()).thenReturn(Optional.empty());

        TranscodeResult result = subject.transcode(task);
        assertThat(result.isSuccessful()).isFalse();
    }

}