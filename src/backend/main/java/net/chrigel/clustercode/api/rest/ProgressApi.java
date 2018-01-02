package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.api.cache.ProgressCache;
import net.chrigel.clustercode.api.dto.ApiError;
import net.chrigel.clustercode.api.dto.FfmpegProgressReport;
import net.chrigel.clustercode.api.dto.HandbrakeProgressReport;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.impl.Transcoder;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(RestApiServices.REST_API_CONTEXT_PATH + "/progress")
@Api(description = "The progress service API")
public class ProgressApi extends AbstractRestApi {

    private final TranscodingService transcodingService;
    private ProgressReportAdapter<FfmpegProgressReport> ffmpegAdapter;
    private ProgressReportAdapter<HandbrakeProgressReport> handbrakeAdapter;
    private final ProgressCache cache;

    @Inject
    ProgressApi(TranscodingService transcodingService,
                ProgressReportAdapter<FfmpegProgressReport> ffmpegAdapter,
                ProgressReportAdapter<HandbrakeProgressReport> handbrakeAdapter,
                ProgressCache cache) {
        this.transcodingService = transcodingService;
        this.ffmpegAdapter = ffmpegAdapter;
        this.handbrakeAdapter = handbrakeAdapter;
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
        if (transcodingService.getTranscoder() != Transcoder.FFMPEG)
            return clientError("This transcoder is not available on the current node.");
        return createResponse(() ->
            cache.getFfmpegOutput()
                 .map(ffmpegAdapter::apply)
                 .orElse(ffmpegAdapter.getReportForInactiveEncoding()));
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
        if (transcodingService.getTranscoder() != Transcoder.HANDBRAKE)
            return clientError("This transcoder is not available on the current node.");
        return createResponse(() ->
            cache.getHandbrakeOutput()
                 .map(handbrakeAdapter::apply)
                 .orElse(handbrakeAdapter.getReportForInactiveEncoding()));
    }
}
