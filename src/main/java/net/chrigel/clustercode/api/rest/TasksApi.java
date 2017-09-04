package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.api.dto.ApiError;
import net.chrigel.clustercode.api.dto.Task;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.ClusterTask;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.stream.Collectors;

@Path(RestApiServices.REST_API_CONTEXT_PATH + "/tasks")
@Api(description = "the tasks API")
public class TasksApi extends AbstractRestApi {

    private final ClusterService clusterService;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

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

