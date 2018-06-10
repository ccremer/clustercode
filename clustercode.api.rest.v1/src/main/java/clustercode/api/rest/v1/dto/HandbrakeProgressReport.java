package clustercode.api.rest.v1.dto;

import clustercode.api.rest.v1.ProgressReport;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(
        description = "A progress report specifically for Handbrake based cluster members.")
public class HandbrakeProgressReport implements ProgressReport {

    @ApiModelProperty(
            value = "The average frames per second in this conversion task so far.",
            example = "22.54")
    private Double averageFps;

    @ApiModelProperty(
            value = "The estimated amount of time to complete.",
            example = "00:51:34")
    private String eta;

    @ApiModelProperty(
            value = "The current amount of frames per second encoding.",
            example = "33")
    private Double fps;

    @ApiModelProperty(value = "The current percentage of the conversion progress.",
            example = "12.1087279")
    private Double percentage;
}
