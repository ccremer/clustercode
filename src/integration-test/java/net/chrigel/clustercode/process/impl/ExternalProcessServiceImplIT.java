package net.chrigel.clustercode.process.impl;

import net.chrigel.clustercode.process.ProcessConfiguration;
import net.chrigel.clustercode.test.CompletableUnitTest;
import net.chrigel.clustercode.test.TestUtility;
import net.chrigel.clustercode.util.Platform;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ExternalProcessServiceImplIT implements CompletableUnitTest {

    private ExternalProcessServiceImpl subject;

    @Before
    public void setUp() {
        subject = new ExternalProcessServiceImpl();
    }

    @Test(timeout = 2000)
    public void start_ShouldCallScriptWithArguments() {
        setExpectedCountForCompletion(3);
        Path executable = getArgumentsScript();
        List<String> expected = Arrays.asList("arg1", "arg2 with spaces");

        ProcessConfiguration c = ProcessConfiguration
            .builder()
            .arguments(expected)
            .executable(executable)
            .stdoutObserver(observable ->
                observable.subscribe(s -> {
                    if (s.contains(" ")) {
                        assertThat(s).isEqualTo("arg1 \"arg2 with spaces\"");
                    } else {
                        assertThat(s).isEqualTo("Arguments:");
                    }
                    assertThat(Thread.currentThread().getName()).startsWith("RxCachedThreadScheduler");
                    completeOne();
                }))
            .build();

        subject.start(c, runningExternalProcess -> {})
               .subscribe(
                   exitCode -> {
                       assertThat(exitCode).isEqualTo(0);
                       assertThat(Thread.currentThread().getName()).startsWith("RxCachedThreadScheduler");
                       completeOne();
                   });
        waitForCompletion();
    }

    @Test(timeout = 2000)
    public void destroy_ShouldStopProcess() {
        setExpectedCountForCompletion(4);
        Path executable = getSleepScript();
        List<String> expected = Collections.singletonList("5000");

        ProcessConfiguration c = ProcessConfiguration
            .builder()
            .arguments(expected)
            .executable(executable)
            .stdoutObserver(observable ->
                observable.subscribe(s -> {
                    assertThat(Thread.currentThread().getName()).startsWith("RxCachedThreadScheduler");
                    completeOne();
                }))
            .build();

        subject
            .start(c, process -> {
                process.sleep(500)
                       .awaitDestruction();
                completeOne();
            })
            .subscribe(exitCode -> {
                assertThat(exitCode).isEqualTo(1);
                completeOne();
            });
        waitForCompletion();
    }


    @Test(timeout = 2000)
    public void start_ShouldThrowException_IfFileDoesNotExist() {
        Path executable = Paths.get("inexistent.file");

        ProcessConfiguration c = ProcessConfiguration
            .builder()
            .executable(executable)
            .build();

        subject
            .start(c)
            .subscribe(
                exitCode -> fail("This should not get called."),
                ex -> {
                    assertThat(ex).isInstanceOf(IOException.class);
                    completeOne();
                });
        waitForCompletion();
    }

    private Path getArgumentsScript() {
        switch (Platform.currentPlatform()) {
            case WINDOWS:
                return TestUtility.getIntegrationTestResourcesDir().resolve("Echo Arguments.cmd").toAbsolutePath();
            default:
                return TestUtility.getIntegrationTestResourcesDir().resolve("Echo Arguments.sh").toAbsolutePath();
        }
    }

    private Path getSleepScript() {
        switch (Platform.currentPlatform()) {
            case WINDOWS:
                return TestUtility.getIntegrationTestResourcesDir().resolve("Sleep.cmd").toAbsolutePath();
            default:
                return TestUtility.getIntegrationTestResourcesDir().resolve("Sleep.sh").toAbsolutePath();
        }
    }
}
