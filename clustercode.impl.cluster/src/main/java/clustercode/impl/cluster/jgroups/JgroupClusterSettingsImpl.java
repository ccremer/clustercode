package clustercode.impl.cluster;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.ToString;

import javax.inject.Named;

@ToString
@Getter
class JgroupClusterSettingsImpl implements JgroupsClusterSettings {

    private String clusterName = "clustercode";
    private String jgroupsConfigFile = "config/tcp.xml";
    private boolean isIPv4Preferred = true;
    private int bindingPort = 7600;
    private String hostname = "";
    private boolean isArbiter = false;
    private String bindingAddress = "127.0.0.1";

    JgroupClusterSettingsImpl() {
        setIPv4Preferred(isIPv4Preferred);
        setBindingPort(bindingPort);
        setBindingAddress(bindingAddress);
    }

    private void checkPort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("Port number must be >= 1");
        }
    }

    //<editor-fold desc="Injected Setters">
    @Inject(optional = true)
    void setArbiter(@Named(ClusterModule.CLUSTER_IS_ARBITER_NODE_KEY) boolean arbiter) {
        this.isArbiter = arbiter;
    }

    @Inject(optional = true)
    void setIPv4Preferred(@Named(ClusterModule.CLUSTER_PREFER_IPV4_KEY) boolean IPv4Preferred) {
        System.setProperty("java.net.preferIPv4Stack", String.valueOf(IPv4Preferred));
        this.isIPv4Preferred = IPv4Preferred;
    }

    @Inject(optional = true)
    void setClusterName(@Named(ClusterModule.CLUSTER_NAME_KEY) String clusterName) {
        this.clusterName = clusterName;
    }

    @Inject(optional = true)
    void setJgroupsConfigFile(@Named(ClusterModule.CLUSTER_JGROUPS_CONFIG_KEY) String jgroupsConfigFile) {
        this.jgroupsConfigFile = jgroupsConfigFile;
    }

    @Inject(optional = true)
    void setHostname(@Named(ClusterModule.CLUSTER_JGROUPS_HOSTNAME_KEY) String hostname) {
        this.hostname = hostname;
    }

    @Inject(optional = true)
    void setBindingPort(@Named(ClusterModule.CLUSTER_JGROUPS_BIND_PORT_KEY) int bindingPort) {
        checkPort(bindingPort);
        System.setProperty("jgroups.bind_port", String.valueOf(bindingPort));
        this.bindingPort = bindingPort;
    }

    @Inject(optional = true)
    void setExternalAddress(@Named(ClusterModule.CLUSTER_JGROUPS_EXTERNAL_ADDR_KEY) String externalAddress) {
        System.setProperty("ext-addr", externalAddress);
    }

    @Inject(optional = true)
    void setInitialHosts(@Named(ClusterModule.CLUSTER_JGROUPS_TCP_INITAL_HOSTS) String initialHosts) {
        System.setProperty("jgroups.tcpping.initial_hosts", initialHosts);
    }

    @Inject(optional = true)
    void setBindingAddress(@Named(ClusterModule.CLUSTER_JGROUPS_BIND_ADDR_KEY) String bindingAddress) {
        System.setProperty("jgroups.bind_addr", bindingAddress);
        this.bindingAddress = bindingAddress;
    }
    //</editor-fold>

}
