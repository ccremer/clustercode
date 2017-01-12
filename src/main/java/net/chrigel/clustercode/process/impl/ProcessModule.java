package net.chrigel.clustercode.process.impl;

import com.google.inject.AbstractModule;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.process.ScriptInterpreter;
import net.chrigel.clustercode.util.FilesystemProvider;
import net.chrigel.clustercode.util.Platform;

import java.nio.file.Files;

public class ProcessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExternalProcess.class).to(ProcessImpl.class);

        switch (Platform.getCurrentPlatform()) {
            case WINDOWS:
                bind(ScriptInterpreter.class).to(AutoResolvableInterpreter.class);
                break;
            default:
                if (Files.exists(FilesystemProvider.getInstance().getPath("/bin", "bash"))) {
                    bind(ScriptInterpreter.class).to(BourneAgainShell.class);
                } else {
                    bind(ScriptInterpreter.class).to(Shell.class);
                }
                break;
        }
    }

}
