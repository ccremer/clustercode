package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.OutputParser;

public class RedirectedParser implements OutputParser {

    private final OutputParser backend;

    public RedirectedParser(OutputParser backend) {
        this.backend = backend;
    }

    @Override
    public void start() {
        backend.start();
    }

    @Override
    public void stop() {
        backend.stop();
    }

    @Override
    public void accept(String line) {
        backend.accept(line);
    }
}
