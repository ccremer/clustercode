package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.test.ClockBasedUnitTest;
import net.chrigel.clustercode.util.InvalidConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TimeConstraintTest implements ClockBasedUnitTest {

    private TimeConstraint subject;

    @Mock
    private Media candidate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void accept_ShouldReturnTrue_IfCurrentTimeIsBetweenBeginAndStop() throws Exception {
        subject = new TimeConstraint("13:00", "14:00",
                getFixedClock(13, 30));

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsBeforeBegin() throws Exception {
        subject = new TimeConstraint("13:00", "14:00",
                getFixedClock(12, 30));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsAfterStop() throws Exception {
        subject = new TimeConstraint("13:00", "14:00",
                getFixedClock(14, 30));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnTrue_IfCurrentTimeIsAfterBegin_AndStopIsBeforeBegin() throws Exception {
        subject = new TimeConstraint("13:00", "11:00",
                getFixedClock(14, 30));

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsBeforeBegin_AndStopIsBeforeBegin() throws Exception {
        subject = new TimeConstraint("13:00", "11:00",
                getFixedClock(12, 30));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void ctor_ShouldThrowException_IfBeginAndStopAreSame() throws Exception {
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
                subject = new TimeConstraint("12:00", "12:00",
                        getFixedClock(12, 30)));
    }

    @Test
    public void ctor_ShouldThrowException_IfConstraintConfiguredIncorrectly() throws Exception {
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
                subject = new TimeConstraint("-1", "-1", Clock.systemDefaultZone()));
    }

}
