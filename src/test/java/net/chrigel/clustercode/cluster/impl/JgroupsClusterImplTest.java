package net.chrigel.clustercode.cluster.impl;

import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.test.MockedFileBasedUnitTest;
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

public class JgroupsClusterImplTest implements MockedFileBasedUnitTest {

    private JgroupsClusterImpl subject;

    @Mock
    private JgroupsClusterSettings settings;
    @Mock
    private Media candidate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new JgroupsClusterImpl(settings, Clock.systemDefaultZone());
    }

    @Test
    public void getCurrentUtcTime_ShouldReturnTimeInUtc() throws Exception {
        subject = new JgroupsClusterImpl(settings, Clock.fixed(Instant.parse("2017-01-01T13:30:00Z"), ZoneOffset.UTC));
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
        boolean result = subject.isFileEquals("0\\movies folder\\movie.mp4", "0/movies folder/movie.mp4");

        assertThat(result).isTrue();
    }

    @Test
    public void isFileEquals_ShouldReturnTrue_WhenComparingPath_FromUnix_ToUnix() throws Exception {
        boolean result = subject.isFileEquals("0/movies folder/movie.mp4", "0/movies folder/movie.mp4");

        assertThat(result).isTrue();
    }

    @Test
    public void isFileEquals_ShouldReturnTrue_WhenComparingPath_FromUnix_ToWindows() throws Exception {
        boolean result = subject.isFileEquals("0/movies folder/movie.mp4", "0\\movies folder\\movie.mp4");

        assertThat(result).isTrue();
    }
}