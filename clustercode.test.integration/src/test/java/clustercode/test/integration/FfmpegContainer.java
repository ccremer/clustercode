package clustercode.test.integration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FfmpegContainer extends GenericContainer {

    @Getter
    private final Path path;

    FfmpegContainer(int durationInSeconds, String filename) {
        super("jrottenberg/ffmpeg:3.4-alpine");
        this.path = Paths.get("input").resolve(filename);
        this.withCommand(
                "-hide_banner", "-y",
                "-t", String.valueOf(durationInSeconds),
                "-f", "rawvideo",
                "-pix_fmt", "rgb24",
                "-r", "25",
                "-s", "640x480",
                "-i", "/dev/zero",
                "/input/" + filename)
            .withFileSystemBind("./input", "/input")
            .waitingFor(new RunOnceWaitStrategy());
    }

}
