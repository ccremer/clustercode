package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.cleanup.CleanupProcessor;
import clustercode.api.process.ExternalProcessService;
import clustercode.api.process.ProcessConfiguration;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.impl.util.Platform;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@XSlf4j
public class ChangeOwnerProcessor implements CleanupProcessor {

    private final CleanupConfig cleanupConfig;
    private final ExternalProcessService externalProcessService;
    private boolean enabled;

    @Inject
    ChangeOwnerProcessor(
        CleanupConfig cleanupConfig,
        ExternalProcessService externalProcessService) {
        this.cleanupConfig = cleanupConfig;
        this.externalProcessService = externalProcessService;
        checkId(cleanupConfig.group_id());
        checkId(cleanupConfig.user_id());
        checkSettings();
    }

    private void checkSettings() {
        enabled = true;
        if (Platform.currentPlatform() == Platform.WINDOWS) {
            log.warn("Changing owner on Windows is not supported. This strategy will be ignored.");
            enabled = false;
        }
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);

        if (!isStepValid(context)) return log.exit(context);

        Path outputFile = context.getOutputPath();
        List<String> args = buildArguments(outputFile);

        log.info("Changing owner of {} to {}.", outputFile, args.get(0));

        ProcessConfiguration config = ProcessConfiguration
            .builder()
            .executable(Paths.get("/bin", "chown"))
            .arguments(args)
            .stdoutObserver(System.out::println)
            .build();

        externalProcessService
            .start(config)
            .subscribe(exitCode -> {
                if (exitCode > 0) log.warn(
                    "Could not change owner of {}. Exit code of 'chown' with arguments {} was {}.",
                    outputFile, args, exitCode);
            }, log::catching);

        return log.exit(context);
    }

    private boolean isStepValid(CleanupContext context) {
        if (!enabled) {
            log.debug("This processor is disabled.");
            return false;
        }

        if (context.getOutputPath() == null) {
            log.warn("Output file has not been created yet. Are you sure you have " +
                "configured the cleanup strategies correctly?");
            return false;
        }
        return true;
    }

    private List<String> buildArguments(Path outputFile) {
        return Arrays.asList(
            cleanupConfig.user_id() + ":" + cleanupConfig.group_id(),
            outputFile.toString());
    }

    private void checkId(int id) {
        if (0 > id) {
            throw new InvalidConfigurationException("Cannot use a negative owner id");
        } else if (id > 65534) {
            throw new InvalidConfigurationException("Cannot use an owner id higher than 65534");
        }
    }
}
