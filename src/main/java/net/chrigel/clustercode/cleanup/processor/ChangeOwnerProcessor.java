package net.chrigel.clustercode.cleanup.processor;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.util.InvalidConfigurationException;
import net.chrigel.clustercode.util.LogUtil;
import net.chrigel.clustercode.util.Platform;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@XSlf4j
public class ChangeOwnerProcessor implements CleanupProcessor {

    private final CleanupSettings cleanupSettings;
    private final Provider<ExternalProcess> externalProcessProvider;

    @Inject
    ChangeOwnerProcessor(
            CleanupSettings cleanupSettings,
            Provider<ExternalProcess> externalProcessProvider) {
        this.cleanupSettings = cleanupSettings;
        this.externalProcessProvider = externalProcessProvider;
        checkId(cleanupSettings.getGroupId());
        checkId(cleanupSettings.getUserId());
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);

        if (context.getOutputPath() == null)
            return LogUtil.logWarnAndExit(context, log,
                    "Output file has not been created yet. Are you sure you have " +
                            "configured the cleanup strategies correctly?");

        if (Platform.currentPlatform() == Platform.WINDOWS)
            return LogUtil.logWarnAndExit(context, log, "Cowardly refusing to change the owner on Windows.");

        Path outputFile = context.getOutputPath();
        List<String> args = buildArguments(outputFile);

        log.info("Changing owner of {} to {}.", outputFile, args.get(0));
        Optional<Integer> exitCode = externalProcessProvider.get()
                .withExecutablePath(Paths.get("/bin", "chown"))
                .withIORedirected(true)
                .withArguments(args)
                .start();

        exitCode.ifPresent(code -> {
            if (code > 0) log.warn(
                    "Could not change owner of {}. Exit code of 'chown' with arguments {} was {}.",
                    outputFile, args, code);
        });

        return log.exit(context);
    }

    private List<String> buildArguments(Path outputFile) {
        return Arrays.asList(
                cleanupSettings.getUserId() + ":" + cleanupSettings.getGroupId(),
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
