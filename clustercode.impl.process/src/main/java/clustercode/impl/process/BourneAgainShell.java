package clustercode.impl.process;

import clustercode.api.process.ScriptInterpreter;
import clustercode.impl.util.FilesystemProvider;

import java.nio.file.Path;

public class BourneAgainShell implements ScriptInterpreter {

    @Override
    public Path getPath() {
        return FilesystemProvider.getInstance().getPath("/bin", "bash");
    }
}
