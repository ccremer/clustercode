package clustercode.impl.cluster.jgroups;

import org.aeonbits.owner.Config;

public interface JgroupsClusterConfig extends Config {

    /**
     * Gets the cluster name.
     *
     * @return the name of the cluster, not null.
     */
    @Key("CC_CLUSTER_NAME")
    @DefaultValue("clustercode")
    String cluster_name();

    /**
     * Gets the file name of the jgroups xml configuration file. Can be absolute, relative or a resource in the JAR
     * file.
     *
     * @return the path to the file, not null.
     */
    @Key("CC_CLUSTER_JGROUPS_CONFIG")
    @DefaultValue("config/tcp.xml")
    String jgroups_config_file();

    /**
     * Whether IPv4 is preferred over IPv6.
     */
    @Key("CC_CLUSTER_JGROUPS_PREFER_IPV4")
    @DefaultValue("true")
    boolean ipv4_preferred();

    /**
     * Gets the address to which the jgroups socket(s) should bind. May return an empty or null string, in IPv4 or IPv6.
     *
     * @return
     */
    @Key("CC_CLUSTER_JGROUPS_BIND_ADDR")
    @DefaultValue("SITE_LOCAL")
    String binding_address();

    @Key("CC_CLUSTER_JGROUPS_EXT_ADDR")
    @DefaultValue("")
    String external_address();

    @Key("CC_CLUSTER_JGROUPS_TCP_INITIAL_HOSTS")
    @DefaultValue("localhost[7600]")
    String initial_hosts();

    /**
     * Gets the host name of this node to be used in the cluster. Can be empty or null, in which case a random host name
     * will be created.
     *
     * @return
     */
    @Key("CC_CLUSTER_JGROUPS_HOSTNAME")
    @DefaultValue("")
    String hostname();

    /**
     * Gets the port number to which the application should listen.
     *
     * @return the port number, {@literal >= 1}.
     */
    @Key("CC_CLUSTER_JGROUPS_BIND_PORT")
    @DefaultValue("7600")
    int getBindingPort();


    /**
     * Indicates whether this current java process is an arbiter node. An arbiter node does not participate in the
     * cluster scheduling and transcoding process, but is just there to provide a cluster quorum.
     *
     * @return true if arbiter enabled, otherwise false.
     */
    @Key("CC_ARBITER_NODE")
    @DefaultValue("false")
    boolean arbiter_enabled();
}
