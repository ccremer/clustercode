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
public class StatusReport {

    @ApiModelProperty(value = "The state in which clustercode is currently in. It is either one of the following: " +
            "INITIAL, SCAN_MEDIA, WAIT, SELECT_MEDIA, SELECT_PROFILE, TRANSCODE, CLEANUP",
            allowableValues = "INITIAL, SCAN_MEDIA, WAIT, SELECT_MEDIA, SELECT_PROFILE, TRANSCODE, CLEANUP")
    private String state;

    @ApiModelProperty(value = "The current amount of cluster members. The size includes any arbiter nodes.")
    private Integer clusterSize;

}

