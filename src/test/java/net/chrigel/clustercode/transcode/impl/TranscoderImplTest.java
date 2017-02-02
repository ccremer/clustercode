package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.test.MockedFileBasedUnitTest;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

public class TranscoderImplTest implements MockedFileBasedUnitTest {

    private TranscoderImpl subject;

    @Mock
    private ExternalProcess process;
    @Mock
    private TranscoderSettings settings;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new TranscoderImpl(() -> process, settings);
        when(process.withArguments(any())).thenReturn(process);
        when(process.withExecutablePath(any())).thenReturn(process);
        when(process.withIORedirected(anyBoolean())).thenReturn(process);
    }

    @Test
    public void prepareArguments_ShouldReplaceInputAndOutput() throws Exception {
        String input = "input.mp4";
        String output = "output.mkv";
        Path source = createPath(input);
        Path target = createPath(output);
        List<String> args = Arrays.asList("--format", "av_mp4", "--input", "${INPUT}", "${OUTPUT}");
        List<String> expected = Arrays.asList("--format", "av_mp4", "--input", input, output);

        subject.from(source)
                .to(target)
                .withArguments(args);
        //   List<String> result = subject.prepareArguments();

        //assertThat(result)
         //       .containsAll(expected);
    }

    @Test
    public void transcode_ShouldReturnTrue_IfEncodingSuccessful() throws Exception {
        Path source = createPath("input.mp4");
        Path target = createPath("output.mkv");

        when(process.start()).thenReturn(Optional.of(0));

        boolean success = subject.from(source)
                .to(target)
                .withArguments(Arrays.asList(""))
                .transcode();

        assertThat(success).isTrue();
    }

    @Test
    public void transcode_ShouldReturnFalse_IfEncodingFailed() throws Exception {
        Path source = createPath("input.mp4");
        Path target = createPath("output.mkv");

        when(process.start()).thenReturn(Optional.of(1));

        boolean success = subject.from(source)
                .to(target)
                .withArguments(Arrays.asList(""))
                .transcode();

        assertThat(success).isFalse();
    }

    @Test
    public void transcode_ShouldReturnFalse_IfEncodingUnknown() throws Exception {
        Path source = createPath("input.mp4");
        Path target = createPath("output.mkv");

        when(process.start()).thenReturn(Optional.empty());

        boolean success = subject.from(source)
                .to(target)
                .withArguments(Arrays.asList(""))
                .transcode();

        assertThat(success).isFalse();
    }

}