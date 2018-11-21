package clustercode.test.integration;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SuppressWarnings("unchecked")
public abstract class AbstractDockerTestBase {

    protected Path createBlankVideo(int durationInSeconds) {
        log.debug("Starting ffmpeg instance1 and create blank video");
        Path target = Paths.get("input", "0", "blank_video_" + durationInSeconds + ".mp4");
        var ffmpeg = (GenericContainer) new GenericContainer("jrottenberg/ffmpeg:3.4-alpine")
                .withCommand(
                        "-hide_banner", "-y",
                        "-t", String.valueOf(durationInSeconds),
                        "-f", "rawvideo",
                        "-pix_fmt", "rgb24",
                        "-r", String.valueOf(25),
                        "-s", "640x480",
                        "-i", "/dev/zero",
                        "/" + target.toString())
                .withFileSystemBind("./input", "/input");
        ffmpeg.start();
        ffmpeg.followOutput(new Slf4jLogConsumer(log));
        log.debug("Startup of Ffmpeg instance1 complete");
        return target;
    }
}
