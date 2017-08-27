package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.dto.ApiError;
import net.chrigel.clustercode.api.dto.ProgressReport;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.TranscodingService;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/progress")
@Api(description = "The progress service API")
@XSlf4j
public class ProgressApi extends AbstractRestApi {

    private final TranscodingService transcodingService;

    @Inject
    ProgressApi(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JSONP
    @ApiOperation(
            value = "Conversion progress",
            notes = "Returns a report of the current conversion progress of the local node.",
            response = ProgressReport.class,
            tags = {"Progress"})
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "A ProgressReport object which contains all relevant values.",
                    response = ProgressReport.class),
            @ApiResponse(
                    code = 500,
                    message = "Unexpected error",
                    response = ApiError.class)})
    public Response getProgress() {
        return createResponse(() -> transcodingService.getCurrentProgress()
                .map(this::convertToDto)
                .orElse(getReportForInactiveTranscoding()));
    }

    private ProgressReport convertToDto(TranscodeProgress transcodeProgress) {
        return ProgressReport.builder()
                .size(transcodeProgress.getFileSize())
                .percentage(transcodeProgress.getPercentage())
                .maxFrame(transcodeProgress.getMaxFrame())
                .fps(transcodeProgress.getFps())
                .bitrate(transcodeProgress.getBitrate())
                .frame(transcodeProgress.getFrame())
                .build();
    }

    ProgressReport getReportForInactiveTranscoding() {
        return ProgressReport.builder()
                .bitrate(-1d)
                .fps(-1d)
                .frame(-1L)
                .maxFrame(-1L)
                .size(-1d)
                .percentage(-1d)
                .build();
    }
}

