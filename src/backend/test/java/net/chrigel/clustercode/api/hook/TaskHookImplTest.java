package net.chrigel.clustercode.api.hook;

import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.cluster.messages.CancelTaskApiRequest;
import net.chrigel.clustercode.cluster.messages.ClusterTaskCollectionChanged;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.event.RxEventBusImpl;
import net.chrigel.clustercode.test.CompletableUnitTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskHookImplTest implements CompletableUnitTest {

    private RxEventBus eventBus;
    private TaskHookImpl subject;

    @Before
    public void setUp() throws Exception {
        eventBus = new RxEventBusImpl();

        subject = new TaskHookImpl(eventBus);
    }

    @Test
    public void getClusterTasks_ShouldReturnEmptyList_IfNoTasksActive() {
        assertThat(subject.getClusterTasks()).isEmpty();
    }

    @Test
    public void getClusterTasks_ShouldReturnTask_IfTaskEmittedFromEventBus() {

        ClusterTask expected = ClusterTask
            .builder()
            .build();
        ClusterTaskCollectionChanged collectionChanged = ClusterTaskCollectionChanged
            .builder()
            .tasks(expected).build();

        eventBus.emit(collectionChanged);

        assertThat(subject.getClusterTasks()).contains(expected);
    }

    @Test
    public void cancelTask_ShouldReturnTrue_IfCancelled() {
        String hostname = "hostname";

        eventBus.register(CancelTaskApiRequest.class)
                .subscribe(r -> {
                    r.setCancelled(true);
                    assertThat(r.getHostname()).isEqualTo(hostname);
                });

        assertThat(subject.cancelTask(hostname)).isTrue();
    }

    @Test(timeout = 1000)
    public void cancelTask_ShouldReturnFalse_IfNotCancelled() {
        String hostname = "hostname";

        eventBus.register(CancelTaskApiRequest.class)
                .subscribe(r -> {
                    r.setCancelled(false);
                    assertThat(r.getHostname()).isEqualTo(hostname);
                    completeOne();
                });

        assertThat(subject.cancelTask(hostname)).isFalse();
        waitForCompletion();
    }
}
