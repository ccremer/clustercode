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
     * The absolute time when this task was created for scheduling.
     */
    private ZonedDateTime dateAdded;

    /**
     * The absolute time when this task was last updated.
     */
    private ZonedDateTime lastUpdated;

    /**
     * The progress in percentage of the task.
     */
    private double percentage;

    /**
     * The name of the host that currently transcodes this task.
     */
    private String memberName;

}
