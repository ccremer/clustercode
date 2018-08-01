package clustercode.main.modules;

import clustercode.api.process.ExternalProcessService;
import clustercode.api.process.ScriptInterpreter;
import clustercode.impl.process.AutoResolvableInterpreter;
import clustercode.impl.process.BourneAgainShell;
import clustercode.impl.process.ExternalProcessServiceImpl;
import clustercode.impl.process.Shell;
import clustercode.impl.util.FilesystemProvider;
import clustercode.impl.util.Platform;
import com.google.inject.AbstractModule;

import java.nio.file.Files;

public class ProcessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExternalProcessService.class).to(ExternalProcessServiceImpl.class);

        switch (Platform.currentPlatform()) {
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
