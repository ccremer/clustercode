package net.chrigel.clustercode.process.impl;

import net.chrigel.clustercode.test.TestUtility;
import net.chrigel.clustercode.util.Platform;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessImplIT {

    private ProcessImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new ProcessImpl();
    }

    @Test
    public void start_ShouldInvokeScriptWithArguments() throws Exception {
        Path executable = getArgumentsScript();
        subject.withExecutablePath(executable).withIORedirected(true);
        subject.withArguments(Arrays.asList("arg1", "arg2 with spaces"));
        Optional<Integer> exitCode = subject.start();
        assertThat(exitCode.get()).isEqualTo(0);
    }

    @Test
    public void start_ShouldTerminateCorrectly() throws Exception {
        Path executable = getSleepScript();
        subject.withExecutablePath(executable).withIORedirected(true);
        subject.withArguments(Arrays.asList("1000"));
        Optional<Integer> exitCode = subject.start();
        assertThat(exitCode.get()).isEqualTo(0);
    }

    @Test
    public void destroyAfter_ShouldDestroyProcess() throws Exception {
        Semaphore blocker = new Semaphore(0);
        subject.withExecutablePath(getSleepScript())
                .withIORedirected(true)
                .withArguments(Arrays.asList("1000"))
                .start(result -> {
                    assertThat(result.isPresent()).isFalse();
                    blocker.release();
                })
                .destroyAfter(500);
        blocker.acquire();
    }

    private Path getArgumentsScript() {
        switch (Platform.getCurrentPlatform()) {
            case WINDOWS:
                return TestUtility.getTestResourcesDir().resolve("Echo Arguments.cmd").toAbsolutePath();
            default:
                return TestUtility.getTestResourcesDir().resolve("Echo Arguments.sh").toAbsolutePath();
        }
    }

    private Path getSleepScript() {
        switch (Platform.getCurrentPlatform()) {
            case WINDOWS:
                return TestUtility.getTestResourcesDir().resolve("Sleep.cmd").toAbsolutePath();
            default:
                return TestUtility.getTestResourcesDir().resolve("Sleep.sh").toAbsolutePath();
        }
    }
}
