package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.*;
import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.api.RestApiServices;
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

    @Inject
    ProgressApi(TranscodingService transcodingService,
                ProgressReportAdapter<FfmpegProgressReport> ffmpegAdapter,
                ProgressReportAdapter<HandbrakeProgressReport> handbrakeAdapter) {
        this.transcodingService = transcodingService;
        this.ffmpegAdapter = ffmpegAdapter;
        this.handbrakeAdapter = handbrakeAdapter;
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
        return createResponse(() -> transcodingService.getProgressCalculator()
                .getProgress()
                .map(this::getPercentage)
                .orElse(-1d));
    }

    private double getPercentage(TranscodeProgress transcodeProgress) {
        return transcodeProgress.getPercentage();
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
        return createResponse(() -> transcodingService.getProgressCalculator()
                .getProgress()
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
        return createResponse(() -> transcodingService.getProgressCalculator()
                .getProgress()
                .map(handbrakeAdapter::apply)
                .orElse(handbrakeAdapter.getReportForInactiveEncoding()));
    }
}

