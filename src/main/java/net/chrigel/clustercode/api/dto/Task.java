package net.chrigel.clustercode.api.dto;

import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {

    @ApiModelProperty(value = "The path under the root input directory.",
            example = "0/movies/amovie.mp4", required = true)
    private String source;

    @ApiModelProperty(value = "The priority of the source file.", example = "1", required = true)
    private Integer priority;

    @JsonProperty
    @JsonDateFormat(value = "yyyy-MM-dd'T'HH:mm:ssZ")
    @ApiModelProperty(value = "The timestamp at which the task has been added.",
            example = "2017-08-27T05:45:12+0200")
    private Date added;

}

