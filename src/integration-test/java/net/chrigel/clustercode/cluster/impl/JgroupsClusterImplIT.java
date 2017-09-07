package net.chrigel.clustercode.cluster.impl;

import net.chrigel.clustercode.cluster.JGroupsMessageDispatcher;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.test.MockedFileBasedUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class JgroupsClusterImplIT implements MockedFileBasedUnitTest {

    private JgroupsClusterImpl subject;

    @Mock
    private JgroupsClusterSettings settings;
    @Mock
    private Media candidate;
    @Mock
    private JGroupsMessageDispatcher dispatcher;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new JgroupsClusterImpl(settings, Clock.systemDefaultZone(), dispatcher);
        when(settings.getJgroupsConfigFile()).thenReturn("docker/default/config/tcp.xml");
        when(settings.getClusterName()).thenReturn("clustercode");
        when(settings.getBindingPort()).thenReturn(5000);
        when(settings.isIPv4Preferred()).thenReturn(true);
        when(settings.getBindingAddress()).thenReturn("");
        when(settings.getHostname()).thenReturn("");
    }

    @After
    public void tearDown() throws Exception {
        subject.leaveCluster();
    }

    @Test
    public void setTask_ShouldAcceptTask() throws Exception {
        Path source = createPath("0", "movie.mp4");
        when(candidate.getSourcePath()).thenReturn(source);

        subject.joinCluster();
        subject.setTask(candidate);
        assertThat(subject.isQueuedInCluster(candidate)).isTrue();
    }


}
