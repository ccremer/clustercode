package net.chrigel.clustercode.cluster.impl;

import net.chrigel.clustercode.cluster.ClusterSettings;

public interface JgroupsClusterSettings extends ClusterSettings {

    String getJgroupsConfigFile();

    boolean isIPv4Preferred();

    String getBindingAddress();

    String getHostname();

    int getBindingPort();

}
