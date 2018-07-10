package clustercode.api.cluster;

import org.jgroups.JChannel;

public interface JGroupsForkService {

    void initialize(JChannel channel, String hostname) throws Exception;
}
