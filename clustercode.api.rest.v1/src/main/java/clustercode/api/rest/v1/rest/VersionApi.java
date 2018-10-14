package clustercode.api.rest.v1.rest;

import clustercode.api.event.messages.StartupCompletedEvent;
import clustercode.api.rest.v1.RestServiceConfig;
import clustercode.api.rest.v1.dto.VersionInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Synchronized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path(RestServiceConfig.REST_API_CONTEXT_PATH + "/version")
public class VersionApi extends AbstractRestApi {

    private static StartupCompletedEvent versionInfo;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            value = "Version information",
            notes = "Provides version information",
            response = VersionInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "An object which contains the version information of the running " +
                    "instance", response = VersionInfo.class),
            @ApiResponse(code = 500, message = "Startup not completed yet")})
    public Response getVersionInfo() {
        if (versionInfo == null) return serverError("Startup not completed yet.");
        return createResponse(() ->
                VersionInfo.builder()
                           .mainVersion(versionInfo.getMainVersion())
                           .build());
    }

    @Synchronized
    public static void setVersion(StartupCompletedEvent event) {
        versionInfo = event;
    }

}
