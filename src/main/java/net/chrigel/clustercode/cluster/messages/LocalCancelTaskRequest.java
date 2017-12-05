package net.chrigel.clustercode.cluster.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalCancelTaskRequest implements ClusterMessage {

    private String hostname;

}
