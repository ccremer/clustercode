package net.chrigel.clustercode.process.impl;

import net.chrigel.clustercode.process.ScriptInterpreter;
import net.chrigel.clustercode.util.FilesystemProvider;

import java.nio.file.Path;

public class BourneAgainShell implements ScriptInterpreter {

    @Override
    public Path getPath() {
        return FilesystemProvider.getInstance().getPath("/bin", "bash");
    }
}
