package clustercode.test.integration;

import clustercode.api.rest.v1.dto.Task;
import clustercode.impl.util.FileUtil;
import lombok.extern.slf4j.XSlf4j;
import lombok.var;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.output.Slf4jLogConsumer;


import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@XSlf4j
public class DockerTestIT {

    @SuppressWarnings("unchecked")
    @ClassRule
    public static ClustercodeContainer container = (ClustercodeContainer)
            new ClustercodeContainer(7700)
                    .withEnv("CC_CONSTRAINTS_ACTIVE", "NONE");

    @BeforeClass
    public static void setup() throws IOException {
        var target = Paths.get("input", "0", "blank_video.mp4");
        var source = Paths.get("clustercode.test.integration", "src", "test", "resources", "blank_video.mp4");
        if (!Files.exists(target)) {
        //    FileUtil.createDirectoriesFor(target.getParent());
        //    Files.copy(source, target);
        }
    }

    @Test
    public void testContainerStartup() {
        assertThat(container.get("/api/v1/tasks").readEntity(new GenericType<List<Task>>() {
        }))
                .isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void followOutput() {
        container.followOutput(new Slf4jLogConsumer(log));
    }
}
