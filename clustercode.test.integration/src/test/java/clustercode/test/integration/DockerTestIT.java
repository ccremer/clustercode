package clustercode.test.integration;

import lombok.extern.slf4j.XSlf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;

@XSlf4j
public class DockerTestIT {

    @SuppressWarnings("unchecked")
    @ClassRule
    public static GenericContainer container = (GenericContainer)
            new GenericContainer("braindoctor/clustercode:amd64")
                    .withExposedPorts(7700)
                    .withEnv("CC_CONSTRAINTS_ACTIVE", "NONE")
                    .withStartupCheckStrategy(new HttpStartupCheckStrategy(
                            7700, "/api/v1/progress", response -> {

                        if (response.readEntity(Integer.class) == -1L) return StartupCheckStrategy.StartupStatus.SUCCESSFUL;
                        return StartupCheckStrategy.StartupStatus.NOT_YET_KNOWN;
                    }))
                    .withFileSystemBind("/etc/localtime", "/etc/localtime", BindMode.READ_ONLY)
                    .withFileSystemBind("./input", "/input")
                    .withFileSystemBind("./output", "/output");

    @Test
    public void testRedis() {
        followOutput();
    }

    @SuppressWarnings("unchecked")
    private void followOutput() {
        container.followOutput(new Slf4jLogConsumer(log));
    }
}
