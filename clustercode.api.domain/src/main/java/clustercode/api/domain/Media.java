package clustercode.api.domain;

import lombok.*;

import java.nio.file.Path;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = "priority")
public class Media {

    /**
     * The file sourcePath which is relative to the base input dir.
     */
    private Path sourcePath;

    /**
     * The priority of the media candidate, {@literal >= 0}, where 0 means lowest priority.
     */
    private int priority;

}
