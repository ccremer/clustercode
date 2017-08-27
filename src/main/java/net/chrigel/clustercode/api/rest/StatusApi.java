package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.StateMachineMonitor;
import net.chrigel.clustercode.api.dto.ApiError;
import net.chrigel.clustercode.api.dto.StatusReport;
import net.chrigel.clustercode.cluster.ClusterService;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@XSlf4j
@Path("/status")
@Api(description = "the status API")
public class StatusApi extends AbstractRestApi {

    private final ClusterService clusterService;
    private final StateMachineMonitor stateMachineMonitor;

    @Inject
    StatusApi(ClusterService clusterService,
              StateMachineMonitor stateMachineMonitor) {
        this.clusterService = clusterService;
        this.stateMachineMonitor = stateMachineMonitor;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(value = "Status information", notes = "Provides operations to monitor the health of the application" +
            ". Can be used for service monitoring.", response = StatusReport.class, tags = {"Health"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A StatusReport object which contains all relevant values.", response
                    = StatusReport.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = ApiError.class)})
    public Response getStatus() {
        return createResponse(() -> StatusReport.builder()
                .clusterSize(clusterService.getSize())
                .state(stateMachineMonitor.getCurrentState().name())
                .build());
    }
}

