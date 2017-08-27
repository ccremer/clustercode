package net.chrigel.clustercode.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.scan.MediaScanSettings;

import java.io.Serializable;
import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ClusterTask implements Serializable {

    /**
     * This is the relative path to {@link MediaScanSettings#getBaseInputDir()} which a node is currently converting.
     */
    private String sourceName;

    /**
     * This represents the priority of the media file.
     */
    private int priority;

    /**
     * The absolute time when this cleanup was created for scheduling.
     */
    private ZonedDateTime dateAdded;

}
