package clustercode.impl.constraint;

import clustercode.api.domain.Media;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.test.util.ClockBasedUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

public class TimeConstraintTest implements ClockBasedUnitTest {

    private TimeConstraint subject;

    @Mock
    private Media candidate;
    @Mock
    private ConstraintConfig config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void accept_ShouldReturnTrue_IfCurrentTimeIsBetweenBeginAndStop() throws Exception {
        when(config.time_begin()).thenReturn("13:00");
        when(config.time_stop()).thenReturn("14:00");
        subject = new TimeConstraint(config, getFixedClock(13, 30));

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsBeforeBegin() throws Exception {
        when(config.time_begin()).thenReturn("13:00");
        when(config.time_stop()).thenReturn("14:00");
        subject = new TimeConstraint(config, getFixedClock(12, 30));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsAfterStop() throws Exception {
        when(config.time_begin()).thenReturn("13:00");
        when(config.time_stop()).thenReturn("14:00");
        subject = new TimeConstraint(config, getFixedClock(14, 30));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnTrue_IfCurrentTimeIsAfterBegin_AndStopIsBeforeBegin() throws Exception {
        when(config.time_begin()).thenReturn("13:00");
        when(config.time_stop()).thenReturn("12:00");
        subject = new TimeConstraint(config, getFixedClock(14, 30));

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsBeforeBegin_AndStopIsBeforeBegin() throws Exception {
        when(config.time_begin()).thenReturn("13:00");
        when(config.time_stop()).thenReturn("11:00");
        subject = new TimeConstraint(config, getFixedClock(12, 30));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void ctor_ShouldThrowException_IfBeginAndStopAreSame() throws Exception {
        when(config.time_begin()).thenReturn("12:00");
        when(config.time_stop()).thenReturn("12:00");
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
                subject = new TimeConstraint(config, getFixedClock(12, 30)));
    }

    @Test
    public void ctor_ShouldThrowException_IfConstraintConfiguredIncorrectly() throws Exception {
        when(config.time_begin()).thenReturn("-1");
        when(config.time_stop()).thenReturn("-1");
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
                subject = new TimeConstraint(config, Clock.systemDefaultZone()));
    }

}
