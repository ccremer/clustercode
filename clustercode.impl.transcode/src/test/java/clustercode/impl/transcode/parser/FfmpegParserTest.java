package clustercode.impl.transcode.parser;

import clustercode.api.transcode.output.FfmpegOutput;
import clustercode.test.util.CompletableUnitTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class FfmpegParserTest implements CompletableUnitTest {

    private FfmpegParser subject;

    @Before
    public void setup() {
        this.subject = new FfmpegParser();
    }

    @Ignore("Does not work currently")
    @Test
    public void parse_ShouldParseLine_AndNotifyObservers() throws Exception {
        String line = "frame=81624 fps= 33 q=-0.0 Lsize= 1197859kB time=00:56:44.38 bitrate=2882.4kbits/s speed=1.36x";

        subject.onProgressParsed()
               .cast(FfmpegOutput.class)
               .subscribe(p -> {
                   assertThat(p.getFrame()).isEqualTo(81624);
                   assertThat(p.getBitrate()).isEqualTo(2882.4);
                   assertThat(p.getSpeed()).isEqualTo(1.36);
                   assertThat(p.getFps()).isEqualTo(33);
                   completeOne();
               });

        subject.parse(line);

        waitForCompletion();
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
