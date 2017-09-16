package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.*;
import lombok.val;
import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.api.dto.ApiError;
import net.chrigel.clustercode.api.dto.Task;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.ClusterTask;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.stream.Collectors;

@Path(RestApiServices.REST_API_CONTEXT_PATH + "/tasks")
@Api(description = "the tasks API")
public class TasksApi extends AbstractRestApi {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final ClusterService clusterService;

    @Inject
    TasksApi(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(
        value = "Tasks information",
        notes = "Provides operations to get task information. Completed tasks do not appear in the list.",
        response = Task.class, responseContainer = "List", tags = {"Tasks"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An Array of Task object.", response = Task.class, responseContainer =
            "List"),
        @ApiResponse(code = 500, message = "Unexpected error", response = ApiError.class)})
    public Response getTasks() {
        return createResponse(() -> clusterService.getTasks().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()));
    }

    @DELETE
    @Path("/stop")
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(
        value = "",
        notes = "Stops the task that is currently being processed by the given hostname. If successful, the affected " +
            "node will transition to state WAIT.",
        response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Stopped the task successfully."),
        @ApiResponse(code = 409, message = "The task has not been found."),
        @ApiResponse(code = 500, message = "Unexpected error", response = ApiError.class)
    })
    public Response stopTask(
        @QueryParam("hostname")
        @ApiParam(value = "host name of the node, as returned by /tasks. If this parameter is omitted, then the node " +
            "of the current API endpoint will cancel its task.")
            String hostname
    ) {
        log.debug("Hostname: {}", hostname);
        try {
            val cancelled = clusterService.cancelTask(hostname);
            if (cancelled) return Response.ok().build();
            return Response.status(Response.Status.CONFLICT).build();
        } catch (Exception ex) {
            log.catching(ex);
            return Response.serverError()
                .entity(ApiError.builder()
                    .message(ex.getMessage())
                    .build())
                .build();
        }
    }

    private Task convertToDto(ClusterTask clusterTask) {
        return Task.builder()
            .priority(clusterTask.getPriority())
            .source(clusterTask.getSourceName())
            .added(Date.from(clusterTask.getDateAdded().toInstant()))
            .updated(Date.from(clusterTask.getLastUpdated().toInstant()))
            .nodename(clusterTask.getMemberName())
            .progress(Double.parseDouble(decimalFormat.format(clusterTask.getPercentage())))
            .build();
    }
}

