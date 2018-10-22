package clustercode.impl.transcode.parser;

import clustercode.api.transcode.output.HandbrakeOutput;
import clustercode.test.util.CompletableUnitTest;
import lombok.var;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HandbrakeParserTest implements CompletableUnitTest {

    private HandbrakeParser subject;

    @Before
    public void setUp() throws Exception {
        subject = new HandbrakeParser();
    }

    @Test(timeout = 1000)
    public void parse_ShouldParseLine_WithoutDuration() {

        String input = "Encoding: task 1 of 1, 5.11 %";

        subject.onProgressParsed(r -> {
            var p = (HandbrakeOutput) r;
            assertThat(p.getPercentage()).isCloseTo(5.11d, Offset.offset(0.001d));
            assertThat(p.getAverageFps()).isZero();
            assertThat(p.getEta()).isEqualTo("00:00:00");
            assertThat(p.getFps()).isZero();
            completeOne();
        });
        subject.parse(input);
        subject.close();

        waitForCompletion();
    }

    @Test(timeout = 1000)
    public void parse_ShouldParseLine_WithDuration() {

        String input = "Encoding: task 1 of 1, 5.11 % (67.61 fps, avg 67.59 fps, ETA 00h20m43s))";

        subject.onProgressParsed(r -> {
            var p = (HandbrakeOutput) r;
            assertThat(p.getPercentage()).isCloseTo(5.11d, Offset.offset(0.001d));
            assertThat(p.getAverageFps()).isCloseTo(67.59d, Offset.offset(0.001d));
            assertThat(p.getEta()).isEqualTo("00:20:43");
            assertThat(p.getFps()).isCloseTo(67.61d, Offset.offset(0.001d));
            completeOne();
        });

        subject.parse(input);
        subject.close();

        waitForCompletion();
    }

    @Test(timeout = 1000)
    public void parse_ShouldIgnoreInvalidLine() {

        String ignored = "something else";
        String input = "Encoding: task 1 of 1, 5.11 % (67.61 fps, avg 67.59 fps, ETA 00h20m43s))";

        subject.onProgressParsed(r -> completeOne());
        subject.parse(ignored);
        subject.parse(input);

        waitForCompletion();
    }

}
