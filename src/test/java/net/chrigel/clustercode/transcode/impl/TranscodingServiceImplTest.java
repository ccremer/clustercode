package net.chrigel.clustercode.transcode.impl;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import net.chrigel.clustercode.process.ExternalProcessService;
import net.chrigel.clustercode.process.ProcessConfiguration;
import net.chrigel.clustercode.process.RunningExternalProcess;
import net.chrigel.clustercode.transcode.OutputParser;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.test.CompletableUnitTest;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.util.UnsafeCastUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TranscodingServiceImplTest implements FileBasedUnitTest, CompletableUnitTest {

    @Mock
    private ExternalProcessService process;
    @Mock
    private RunningExternalProcess runningProcessMock;
    @Mock
    private TranscoderSettings transcoderSettings;
    @Mock
    private MediaScanSettings mediaScanSettings;
    @Mock
    private OutputParser parser;

    @Spy
    private Media media;
    @Spy
    private Profile profile;
    @Spy
    private TranscodeTask task;

    private TranscodingServiceImpl subject;

    private ProcessConfiguration processConfiguration;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();

        processConfiguration = ProcessConfiguration.builder()
                                                   .executable(getPath("mock"))
                                                   .build();

        when(transcoderSettings.getTranscoderExecutable()).thenReturn(getPath("mock"));
        when(media.getSourcePath()).thenReturn(getPath("0", "video.mkv"));
        when(profile.getFields()).thenReturn(Collections.singletonMap("FORMAT", ".mp4"));
        when(transcoderSettings.getTemporaryDir()).thenReturn(getPath("tmp"));
        when(mediaScanSettings.getBaseInputDir()).thenReturn(getPath("root"));
        when(profile.getArguments()).thenReturn(Collections.emptyList());

        task.setMedia(media);
        task.setProfile(profile);

        subject = new TranscodingServiceImpl(process,
            transcoderSettings,
            mediaScanSettings,
            parser);
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

        assertThat(result).isEqualTo(mediaScanSettings.getBaseInputDir().resolve(input).toString());
    }

    @Test(timeout = 1000)
    public void transcode_ShouldFireEvent_IfTranscodingSuccessful() {
        when(process.start(any(), any())).thenReturn(Single.just(0));

        subject.onTranscodeFinished()
               .subscribe(result -> {
                   assertThat(result.getTemporaryPath().toString()).isEqualTo("tmp\\video.mp4");
                   assertThat(result.isSuccessful()).isTrue();
                   completeOne();
               });
        subject.transcode(task);

        waitForCompletion();
    }

    @Test(timeout = 1000)
    public void transcode_ShouldFireEvent_IfTranscodingFailed() {
        when(process.start(any(), any())).thenReturn(Single.just(1));

        subject.onTranscodeFinished()
               .subscribe(result -> {
                   assertThat(result.isSuccessful()).isFalse();
                   completeOne();
               });
        subject.transcode(task);

        waitForCompletion();
    }

    @Test(timeout = 1000)
    public void transcode_ShouldFireEvent_IfTranscodingFailed_OnException() {
        when(process.start(any(), any())).thenReturn(Single.error(IOException::new));

        subject.onTranscodeFinished()
               .subscribe(result -> {
                   assertThat(result.isSuccessful()).isFalse();
                   completeOne();
               });
        subject.transcode(task);

        waitForCompletion();
    }

    @Test(timeout = 1000)
    public void transcode_ShouldFireEvent_IfTranscodingBegins() {
        when(process.start(any(), any())).thenReturn(Single.just(0));

        subject.onTranscodeBegin()
               .subscribe(result -> {
                   assertThat(result.getTask()).isEqualTo(task);
                   completeOne();
               });
        subject.transcode(task);

        waitForCompletion();
    }

    @Test(timeout = 2000)
    public void cancel_ShouldCancelJob() {
        setExpectedCountForCompletion(2);

        CountDownLatch latch = new CountDownLatch(1);
        when(process.start(any(), any())).thenAnswer(invocation -> Single
            .fromCallable(() -> {
                Consumer<RunningExternalProcess> consumer = UnsafeCastUtil.cast(
                    invocation.getArgumentAt(1, Consumer.class));
                consumer.accept(runningProcessMock);
                latch.countDown();
                subject.cancelTranscode();
                //Thread.sleep(500);
                return 1;
            })
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
        );

        subject.onTranscodeBegin()
               .subscribe(task -> {
                   completeOne();
                   latch.await();
               });
        subject.onTranscodeFinished()
               .subscribe(result -> {
                   assertThat(result.isCancelled()).isTrue();
                   assertThat(result.isSuccessful()).isFalse();
                   completeOne();
               });
        subject.transcode(task);

        waitForCompletion();
    }
}
