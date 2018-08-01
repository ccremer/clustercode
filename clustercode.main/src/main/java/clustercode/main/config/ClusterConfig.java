package clustercode.main.config;

import clustercode.main.modules.ClusterType;
import org.aeonbits.owner.Config;

public interface ClusterConfig extends Config {

    @Key("CC_CLUSTER_TYPE")
    @DefaultValue("JGROUPS")
    ClusterType cluster_type();

}
