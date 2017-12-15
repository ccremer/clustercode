package net.chrigel.clustercode.process.impl;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessImplTest {

    private ProcessImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new ProcessImpl();
    }

    @Test
    public void start_ShouldReturnEmptyResult() throws Exception {
        subject.withExecutablePath(Paths.get("inexistent.file"))
                .withIORedirected(false)
                .withLogSuppressed()
                .withCurrentWorkingDirectory(Paths.get("inexistent.dir"));
        Optional<Integer> exitCode = subject.start();
        assertThat(exitCode.isPresent());
    }

    @Test
    public void sleep_ShouldSleepCurrentThread() throws Exception {
        LocalDateTime before = LocalDateTime.now();
        subject.sleep(200, TimeUnit.MILLISECONDS);
        LocalDateTime after = LocalDateTime.now().minusNanos(100000);
        assertThat(before.isBefore(after)).isTrue();
    }

}
