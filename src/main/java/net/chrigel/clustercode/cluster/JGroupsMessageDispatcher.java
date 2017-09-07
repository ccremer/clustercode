package net.chrigel.clustercode.cluster;

import org.jgroups.JChannel;

public interface JGroupsMessageDispatcher {

    boolean cancelTask(String hostname);

    void initialize(JChannel channel);
}
