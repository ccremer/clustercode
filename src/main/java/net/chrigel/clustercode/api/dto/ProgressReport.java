package net.chrigel.clustercode.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProgressReport {

    @ApiModelProperty(value = "The current percentage of the conversion progress. Returns -1 if there is no " +
            "conversion or if it could not be determined. After a conversion this value resets to -1.",
            example = "12.1087279")
    private Double percentage;

    @ApiModelProperty(value = "The current bitrate of the conversion in kbit/s. Returns -1 if no conversion is active.",
            example = "2543")
    private Double bitrate;

    @ApiModelProperty(value = "The amount of frames per second encoded in the current conversion. Returns -1 if no " +
            "conversion active.",
            example = "33")
    private Double fps;

    @ApiModelProperty(value = "The amount of processed frames so far in the conversion process. Returns -1 if no " +
            "conversion active.",
            example = "2851")
    private Long frame;

    @ApiModelProperty(value = "The frame count of the current media. Returns 0 if not determined and -1 if no " +
            "conversion active.",
            example = "23545")
    private Long maxFrame;

    @ApiModelProperty(value = "The file size of the currently created output file. Returns -1 if no conversion active" +
            ". Otherwise returns a value in MB.",
            example = "20.34")
    private Double size;

}

