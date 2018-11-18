package clustercode.test.integration;

import org.slf4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class ClustercodeContainer extends HttpContainer {

    public ClustercodeContainer(int port) {
        super("braindoctor/clustercode:amd64", port);
        this
                .withExposedPorts(port)
                .waitingFor(new HttpWaitStrategy()
                        .forPath("/api/v1/version")
                        .forPort(port))
                .withEnv("CC_CONSTRAINTS_ACTIVE", "NONE")
                .withEnv("LOG_LEVEL_CORE", "debug")
                .withFileSystemBind("/etc/localtime", "/etc/localtime", BindMode.READ_ONLY)
                .withFileSystemBind("./input", "/input")
                .withFileSystemBind("./output", "/output")
                .withFileSystemBind("./profiles", "/profiles");
    }

    protected final ClustercodeContainer withOutputLogger(Logger logger) {
        this.withLogConsumer(new Slf4jLogConsumer(logger));
        return this;
    }

    protected final void waitUntil(Predicate<String> predicate) throws TimeoutException {
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        this.followOutput(waitingConsumer);
        waitingConsumer.waitUntil(frame -> predicate.test(frame.getUtf8String()), 30, TimeUnit.SECONDS);
    }

    protected final void waitUntilLineStartsWith(String beginning) throws TimeoutException {
        waitUntil(line -> {
            // take out the timestamp first.
            if (line.length() > 38) {
                return line.substring(38, line.length() - 1).startsWith(beginning);
            } else {
                return false;
            }
        });
    }

    protected final void waitUntilLineContains(String snippet) throws TimeoutException {
        waitUntil(line -> line.contains(snippet));
    }
}
