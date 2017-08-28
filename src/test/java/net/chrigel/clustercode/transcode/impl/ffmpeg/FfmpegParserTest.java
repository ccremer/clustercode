package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class FfmpegParserTest {

    private FfmpegParser subject;

    @Before
    public void  setup() {
        MockitoAnnotations.initMocks(this);

        this.subject = new FfmpegParser();
    }

    @Test
    public void doParse() throws Exception {

    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_Hours() throws Exception {
        val raw = "11:00:00";

        val result = subject.parseDuration(raw);
        val expected = Duration.ofHours(11);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_Minutes() throws Exception {
        val raw = "00:21:00";

        val result = subject.parseDuration(raw);
        val expected = Duration.ofMinutes(21);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_Seconds() throws Exception {
        val raw = "00:00:05";

        val result = subject.parseDuration(raw);
        val expected = Duration.ofSeconds(5);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_MilliSeconds() throws Exception {
        val raw = "00:00:00.003";

        val result = subject.parseDuration(raw);
        val expected = Duration.ofMillis(3);

        assertThat(result).isEqualByComparingTo(expected);

    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_MixedTime() throws Exception {
        val raw = "11:21:05.345";

        val result = subject.parseDuration(raw);
        val expected = Duration.ofHours(11).plusMinutes(21).plusSeconds(05).plusMillis(345);

        assertThat(result).isEqualByComparingTo(expected);
    }
}