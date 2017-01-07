package net.chrigel.clustercode.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MediaCandidate {

    /**
     * The file sourcePath which is relative to the base input dir.
     */
    private Path sourcePath;

    /**
     * The priority of the media candidate, {@literal >= 0}, where 0 means lowest priority.
     */
    private int priority;

}
