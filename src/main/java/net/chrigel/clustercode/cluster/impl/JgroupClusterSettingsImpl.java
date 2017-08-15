package net.chrigel.clustercode.cluster.impl;

import lombok.ToString;

import javax.inject.Inject;
import javax.inject.Named;

@ToString
class JgroupClusterSettingsImpl implements JgroupsClusterSettings {

    private final String clusterName;
    private final String jgroupsConfig;
    private final boolean preferIPv4;
    private final String bindingAddress;
    private final int bindingPort;
    private final String hostname;
    private final long taskTimeoutHours;
    private final boolean isArbiter;

    @Inject
    JgroupClusterSettingsImpl(@Named(ClusterModule.CLUSTER_NAME_KEY) String clusterName,
                              @Named(ClusterModule.CLUSTER_JGROUPS_CONFIG_KEY) String jgroupsConfig,
                              @Named(ClusterModule.CLUSTER_PREFER_IPV4_KEY) boolean preferIPv4,
                              @Named(ClusterModule.CLUSTER_JGROUPS_BIND_ADDR_KEY) String bindingAddress,
                              @Named(ClusterModule.CLUSTER_JGROUPS_EXTERNAL_ADDR_KEY) String externalAddress,
                              @Named(ClusterModule.CLUSTER_JGROUPS_BIND_PORT_KEY) int bindingPort,
                              @Named(ClusterModule.CLUSTER_JGROUPS_HOSTNAME_KEY) String hostname,
                              @Named(ClusterModule.CC_CLUSTER_JGROUPS_TCP_INITAL_HOSTS) String initialHosts,
                              @Named(ClusterModule.CLUSTER_TASK_TIMEOUT_KEY) long taskTimeoutHours,
                              @Named(ClusterModule.CLUSTER_IS_ARBITER_NODE_KEY) boolean isArbiter) {
        checkPort(bindingPort);
        checkTimeout(taskTimeoutHours);
        this.isArbiter = isArbiter;
        this.clusterName = clusterName;
        this.jgroupsConfig = jgroupsConfig;
        this.preferIPv4 = preferIPv4;
        this.bindingAddress = bindingAddress;
        this.bindingPort = bindingPort;
        this.hostname = hostname;
        this.taskTimeoutHours = taskTimeoutHours;
        System.setProperty("java.net.preferIPv4Stack", String.valueOf(preferIPv4));
        System.setProperty("jgroups.bind_addr", bindingAddress);
        System.setProperty("jgroups.tcpping.initial_hosts", initialHosts);
        System.setProperty("jgroups.bind_port", String.valueOf(bindingPort));
        if (!"-".equals(externalAddress)) System.setProperty("ext-addr", externalAddress);
    }

    private void checkTimeout(long taskTimoutHours) {
        if (taskTimoutHours < 1L) {
            throw new IllegalArgumentException("ClusterItem timeout must be >= 1, was " + taskTimoutHours);
        }
    }

    private void checkPort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("Port number must be >= 1");
        }
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public long getTaskTimeout() {
        return taskTimeoutHours;
    }

    @Override
    public boolean isArbiter() {
        return isArbiter;
    }

    @Override
    public String getJgroupsConfigFile() {
        return jgroupsConfig;
    }

    @Override
    public boolean isIPv4Preferred() {
        return preferIPv4;
    }

    @Override
    public String getBindingAddress() {
        return bindingAddress;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getBindingPort() {
        return bindingPort;
    }
}
