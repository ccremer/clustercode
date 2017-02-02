package net.chrigel.clustercode.cluster.impl;

import net.chrigel.clustercode.cluster.ClusterSettings;

public interface JgroupsClusterSettings extends ClusterSettings {

    /**
     * Gets the cluster name.
     *
     * @return the name of the cluster, not null.
     */
    String getClusterName();

    /**
     * Gets the file name of the jgroups xml configuration file. Can be absolute, relative or a resource in the JAR
     * file.
     *
     * @return the path to the file, not null.
     */
    String getJgroupsConfigFile();

    /**
     * Whether IPv4 is preferred over IPv6.
     *
     * @return
     */
    boolean isIPv4Preferred();

    /**
     * Gets the address to which the jgroups socket(s) should bind. May return an empty or null string, in IPv4 or IPv6.
     *
     * @return
     */
    String getBindingAddress();

    /**
     * Gets the host name of this node to be used in the cluster. Can be empty or null, in which case a random host name
     * will be created.
     *
     * @return
     */
    String getHostname();

    /**
     * Gets the port number to which the application should listen.
     *
     * @return the port number, {@literal >= 1}.
     */
    int getBindingPort();

}
