package clustercode.test.integration;

import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.XSlf4j;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Function;

@XSlf4j
public class HttpStartupCheckStrategy extends StartupCheckStrategy {

    private final Client client;
    private int port;
    private String path;
    private Function<Response, StartupStatus> callback;

    public HttpStartupCheckStrategy(int port, String path, Function<Response, StartupStatus> callback) {
        this.port = port;
        this.path = path;
        this.callback = callback;
        this.client = ClientBuilder.newClient();
    }

    @Override
    public StartupStatus checkStartupState(DockerClient dockerClient, String containerId) {

        String ip = dockerClient.inspectContainerCmd(containerId)
                                .exec()
                                .getNetworkSettings()
                                .getNetworks()
                                .get("bridge")
                                .getIpAddress();

        Response response = client.target(String.format("http://%1$s:%2$s%3$s", ip, port, path))
                           .request(MediaType.APPLICATION_JSON)
                           .get();
        log.info("Got reponse: {}", response);

        return callback.apply(response);
    }
}
