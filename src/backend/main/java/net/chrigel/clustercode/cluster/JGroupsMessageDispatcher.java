package net.chrigel.clustercode.cluster;

public interface JGroupsMessageDispatcher extends JGroupsForkService {

    boolean cancelTask(String hostname);

}
