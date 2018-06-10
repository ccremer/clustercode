package net.chrigel.clustercode.api.rest;

import net.chrigel.clustercode.api.dto.ApiError;
import net.chrigel.clustercode.api.dto.Task;
import net.chrigel.clustercode.api.hook.TaskHook;
import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.util.UnsafeCastUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.ws.rs.core.Response;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TasksApiTest {

    private TasksApi subject;

    @Mock
    private TaskHook hook;

    @Spy
    private ClusterTask task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        task.setLastUpdated(ZonedDateTime.now());
        task.setDateAdded(ZonedDateTime.now());

        subject = new TasksApi(hook);
    }

    @Test
    public void getTasks_ShouldConvertTask_ToDto() {

        Task expected = Task
            .builder()
            .added(Date.from(task.getDateAdded().toInstant()))
            .updated(Date.from(task.getLastUpdated().toInstant()))
            .priority(0)
            .build();

        when(hook.getClusterTasks()).thenReturn(Arrays.asList(task));

        Response response = subject.getTasks();
        List<Task> result = UnsafeCastUtil.cast(response.getEntity());

        assertThat(result).contains(expected);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getTasks_ShouldReturnEmptyList_IfNoTasksActive() {
        when(hook.getClusterTasks()).thenReturn(Collections.emptyList());

        Response response = subject.getTasks();
        List<Task> result = UnsafeCastUtil.cast(response.getEntity());

        assertThat(result).isEmpty();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getTasks_ShouldReturnError_IfExceptionThrown() {
        when(hook.getClusterTasks()).thenThrow(new RuntimeException("message"));

        Response response = subject.getTasks();
        ApiError error = UnsafeCastUtil.cast(response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(error.getMessage()).isEqualTo("message");
    }

    @Test
    public void stopTask_ShouldReturnOk_IfCancelSuccessful() {
        String hostname = "hostname";
        when(hook.cancelTask(hostname)).thenReturn(true);

        Response response = subject.stopTask(hostname);

        verify(hook).cancelTask(hostname);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void stopTask_ShouldReturnError_IfCancelFailed() {
        String hostname = "hostname";
        when(hook.cancelTask(hostname)).thenReturn(false);

        Response response = subject.stopTask(hostname);

        verify(hook).cancelTask(hostname);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void stopTask_ShouldReturnError_IfHostnameIsNull() {
        Response response = subject.stopTask(null);

        verify(hook, never()).cancelTask(any());
        assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());
    }

    @Test
    public void stopTask_ShouldReturnError_IfExceptionThrown() {
        when(hook.cancelTask(any())).thenThrow(new RuntimeException("message"));

        Response response = subject.stopTask("hostname");
        ApiError error = UnsafeCastUtil.cast(response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(error.getMessage()).isEqualTo("message");
    }
}
