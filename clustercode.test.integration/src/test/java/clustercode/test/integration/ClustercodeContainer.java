package clustercode.test.integration;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class ClustercodeContainer extends HttpContainer {

    public ClustercodeContainer(int port) {
        super("braindoctor/clustercode:amd64", port);
        this
                .withExposedPorts(port)
                .waitingFor(new HttpWaitStrategy()
                        .forPath("/api/v1/version")
                        .forPort(port))
                .withFileSystemBind("/etc/localtime", "/etc/localtime", BindMode.READ_ONLY)
                .withFileSystemBind("./input", "/input")
                .withFileSystemBind("./output", "/output");
    }

}
