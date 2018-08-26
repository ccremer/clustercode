package clustercode.api.rest.v1.rest;

import clustercode.api.rest.v1.RestServicesActivator;
import clustercode.api.rest.v1.dto.ApiError;
import clustercode.api.rest.v1.dto.FfmpegProgressReport;
import clustercode.api.rest.v1.dto.HandbrakeProgressReport;
import clustercode.api.rest.v1.hook.ProgressHook;
import clustercode.api.transcode.Transcoder;
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
@Path(RestServicesActivator.REST_API_CONTEXT_PATH + "/progress")
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
        value = "Conversion progress for ffmpeg",
        notes = "Returns a report of the current conversion progress of the local node.",
        response = FfmpegProgressReport.class,
        tags = {"Progress"})
    @ApiResponses(value = {
        @ApiResponse(
            code = 200,
            message = "A FfmpegProgressReport object which contains all relevant values.",
            response = FfmpegProgressReport.class),
        @ApiResponse(
            code = 400,
            message = "This transcoder is not available on the current node.",
            response = ApiError.class
        ),
        @ApiResponse(
            code = 500,
            message = "Unexpected error",
            response = ApiError.class)})
    public Response getFfmpegProgress() {
        if (cache.getTranscoder() != Transcoder.FFMPEG)
            return clientError("This transcoder is not available on the current node.");
        return createResponse(cache::getLatestProgressOutput);
    }

    @Path("handbrake")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP(queryParam = "callback")
    @ApiOperation(
        value = "Conversion progress for handbrake",
        notes = "Returns a report of the current conversion progress of the local node.",
        response = HandbrakeProgressReport.class,
        tags = {"Progress"})
    @ApiResponses(value = {
        @ApiResponse(
            code = 200,
            message = "A HandbrakeProgressAdapter object which contains all relevant values.",
            response = HandbrakeProgressReport.class),
        @ApiResponse(
            code = 400,
            message = "This transcoder is not available on the current node.",
            response = ApiError.class
        ),
        @ApiResponse(
            code = 500,
            message = "Unexpected error",
            response = ApiError.class)})
    public Response getHandbrakeProgress() {
        if (cache.getTranscoder() != Transcoder.HANDBRAKE)
            return clientError("This transcoder is not available on the current node.");
        return createResponse(cache::getLatestProgressOutput);
    }
}

