package clustercode.api.rest.v1.rest;

import clustercode.api.rest.v1.RestServiceConfig;
import clustercode.api.rest.v1.dto.ApiError;
import clustercode.api.rest.v1.hook.ProgressHook;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path(RestServiceConfig.REST_API_CONTEXT_PATH + "/progress")
public class ProgressApi extends AbstractRestApi {

    private final ProgressHook cache;

    @Inject
    ProgressApi(ProgressHook cache) {
        this.cache = cache;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(
        value = "Conversion progress",
        notes = "Gets the percentage of the current encoding process. Returns -1 if no conversion active.",
        response = Double.class,
        tags = {"Progress"})
    @ApiResponses(value = {
        @ApiResponse(
            code = 200,
            message = "OK",
            response = Double.class),
        @ApiResponse(
            code = 500,
            message = "Unexpected error",
            response = ApiError.class)})
    public Response getProgress() {
        return createResponse(cache::getPercentage);
    }

    @Path("ffmpeg")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(
        value = "This URL is not available anymore, use the 2.0 API.",
        response = ApiError.class,
        tags = {"Progress"})
    @ApiResponses(value = {
        @ApiResponse(
            code = 400,
            message = "This URL is not available anymore, use the 2.0 API.",
            response = ApiError.class)})
    public Response getFfmpegProgress() {
        return clientError("This URL is not available anymore, use the 2.0 API.");
    }

    @Path("handbrake")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(
        value = "This URL is not available anymore, use the 2.0 API.",
        response = ApiError.class,
        tags = {"Progress"})
    @ApiResponses(value = {
        @ApiResponse(
            code = 400,
            message = "This URL is not available anymore, use the 2.0 API.",
            response = ApiError.class
        )})
    public Response getHandbrakeProgress() {
        return clientError("This URL is not available anymore, use the 2.0 API.");
    }
}

