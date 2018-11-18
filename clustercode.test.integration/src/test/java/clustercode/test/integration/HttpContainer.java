package clustercode.test.integration;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.testcontainers.containers.GenericContainer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SuppressWarnings("unchecked")
public class HttpContainer extends GenericContainer {

    private JerseyClient client;
    private int port;
    private MediaType defaultMediaType = MediaType.APPLICATION_JSON_TYPE;

    public HttpContainer(String dockerImageName, int port) {
        super(dockerImageName);
        this.port = port;
    }

    public <T> T httpGet(String uri, Class<T> entity) {
        String url = buildUrl(uri);
        logger().debug("Perform http request for {}", url);
        return getClient().target(url)
                          .request(defaultMediaType)
                          .get(entity);
    }

    public Response httpGet(String uri) {
        String url = buildUrl(uri);
        logger().debug("Perform http request for {}", url);
        return getClient().target(url)
                          .request(defaultMediaType)
                          .get();
    }

    public <T> T httpPost(String uri, Class<T> responseType, Entity<?> entity) {
        String url = buildUrl(uri);
        logger().debug("Perform http request for {}", url);
        return getClient().target(url)
                          .request(defaultMediaType)
                          .post(entity, responseType);
    }

    public Response httpPost(String uri, Entity<?> entity) {
        String url = buildUrl(uri);
        logger().debug("Perform http request for {}", url);
        return getClient().target(url)
                          .request(defaultMediaType)
                          .post(entity);
    }

    public JerseyClient getClient() {
        if (client == null) client = JerseyClientBuilder.createClient();
        return client;
    }

    public void setMediaType(MediaType mediaType) {
        this.defaultMediaType = mediaType;
    }

    private String buildUrl(String uri) {
        return String.format("http://%1$s:%2$s%3$s", getContainerIpAddress(), getMappedPort(this.port), uri);
    }
}
