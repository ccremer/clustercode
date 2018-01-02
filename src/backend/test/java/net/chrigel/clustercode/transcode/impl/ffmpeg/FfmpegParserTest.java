package net.chrigel.clustercode.transcode.impl.ffmpeg;

import net.chrigel.clustercode.event.RxEventBusImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class FfmpegParserTest {

    private FfmpegParser subject;

    @Before
    public void  setup() {
        this.subject = new FfmpegParser();
    }

    @Test
    public void doParse() throws Exception {

    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_Hours() throws Exception {
        String raw = "11:00:00";

        Duration result = subject.parseDuration(raw);
        Duration expected = Duration.ofHours(11);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_Minutes() throws Exception {
        String raw = "00:21:00";

        Duration result = subject.parseDuration(raw);
        Duration expected = Duration.ofMinutes(21);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_Seconds() throws Exception {
        String raw = "00:00:05";

        Duration result = subject.parseDuration(raw);
        Duration expected = Duration.ofSeconds(5);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_MilliSeconds() throws Exception {
        String raw = "00:00:00.003";

        Duration result = subject.parseDuration(raw);
        Duration expected = Duration.ofMillis(3);

        assertThat(result).isEqualByComparingTo(expected);

    }

    @Test
    public void parseDuration_ShouldReturnDurationOf_MixedTime() throws Exception {
        String raw = "11:21:05.345";

        Duration result = subject.parseDuration(raw);
        Duration expected = Duration.ofHours(11).plusMinutes(21).plusSeconds(05).plusMillis(345);

        assertThat(result).isEqualByComparingTo(expected);
    }
}
