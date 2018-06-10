package net.chrigel.clustercode.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusReport {

    @ApiModelProperty(value = "The state in which clustercode is currently in. It is either one of the following: " +
        "INITIAL, SCAN_MEDIA, WAIT, SELECT_MEDIA, SELECT_PROFILE, TRANSCODE, CLEANUP, ARBITER",
        allowableValues = "INITIAL, SCAN_MEDIA, WAIT, SELECT_MEDIA, SELECT_PROFILE, TRANSCODE, CLEANUP, ARBITER",
        example = "TRANSCODE")
    private String state;

    @ApiModelProperty(value = "The current amount of cluster members. The size includes any arbiter nodes.",
        example = "2")
    private Integer clusterSize;

}

