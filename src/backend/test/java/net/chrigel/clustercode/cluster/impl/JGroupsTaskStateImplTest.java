package net.chrigel.clustercode.cluster.impl;

import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.event.RxEventBusImpl;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class JGroupsTaskStateImplTest implements FileBasedUnitTest {

    private JGroupsTaskStateImpl subject;

    @Mock
    private Media candidate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        subject = new JGroupsTaskStateImpl(Clock.systemDefaultZone(), new RxEventBusImpl());
    }

    @Test
    public void getCurrentUtcTime_ShouldReturnTimeInUtc() throws Exception {
        subject = new JGroupsTaskStateImpl(Clock.fixed(Instant.parse("2017-01-01T13:30:00Z"), ZoneOffset.UTC), new RxEventBusImpl());
        ZonedDateTime time = subject.getCurrentUtcTime();
        assertThat(time.getHour()).isEqualTo(13);
        assertThat(time.getMinute()).isEqualTo(30);
        assertThat(time.getOffset()).isEqualByComparingTo(ZoneOffset.UTC);
    }

    @Test
    public void createTaskFor_ShouldReturnTask_WithRelativePath_FromWindows() throws Exception {
        Path media = Mockito.mock(Path.class);
        when(candidate.getSourcePath()).thenReturn(media);
        when(media.toString()).thenReturn("0\\movies\\movie.mp4");

        ClusterTask clusterTask = subject.createTaskFor(candidate);

        assertThat(clusterTask.getSourceName()).isEqualTo("0/movies/movie.mp4");
    }

    @Test
    public void isFileEquals_ShouldReturnTrue_WhenComparingPath_FromWindows_ToUnix() throws Exception {
        boolean result = subject.fileEquals("0\\movies folder\\movie.mp4", getPath("0/movies folder/movie.mp4"));

        assertThat(result).isTrue();
    }

    @Test
    public void isFileEquals_ShouldReturnTrue_WhenComparingPath_FromUnix_ToUnix() throws Exception {
        boolean result = subject.fileEquals("0/movies folder/movie.mp4", getPath("0/movies folder/movie.mp4"));

        assertThat(result).isTrue();
    }

    @Test
    public void isFileEquals_ShouldReturnTrue_WhenComparingPath_FromUnix_ToWindows() throws Exception {
        boolean result = subject.fileEquals("0/movies folder/movie.mp4", getPath("0\\movies folder\\movie.mp4"));

        assertThat(result).isTrue();
    }
}
