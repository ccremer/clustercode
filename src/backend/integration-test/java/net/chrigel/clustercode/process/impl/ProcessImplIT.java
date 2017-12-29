package net.chrigel.clustercode.process.impl;

import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.process.RunningExternalProcess;
import net.chrigel.clustercode.test.TestUtility;
import net.chrigel.clustercode.util.Platform;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class ProcessImplIT {

    private ProcessImpl subject;
    @Mock
    private OutputParser parser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new ProcessImpl();
    }

    @Test
    public void start_ShouldInvokeScriptWithArguments() throws Exception {
        Path executable = getArgumentsScript();
        subject.withExecutablePath(executable)
               .withIORedirected(true)
               .withStdoutParser(parser)
               //.withArguments(Arrays.asList("5000"));
               .withArguments(Arrays.asList("arg1", "arg2 with spaces"));
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
        RunningExternalProcess process = subject.withExecutablePath(getSleepScript())
                                                .withIORedirected(true)
                                                .withArguments(Arrays.asList("1000"))
                                                .startInBackground();
        process.destroyAfter(500);
        assertThat(process.waitFor()).isEmpty();
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
