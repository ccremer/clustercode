package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.OutputParser;

public class RedirectedParser<T> extends AbstractOutputParser<T> {

    private final OutputParser<T> backend;

    public RedirectedParser(OutputParser<T> backend) {
        this.backend = backend;
    }

    @Override
    protected T doParse(String line) {
        backend.parse(line);
        return backend.getResult().orElse(null);
    }

    @Override
    protected void doStart() {
        backend.start();
    }

    @Override
    protected void doStop() {
        backend.stop();
    }

}
