package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.OutputParser;

public class RedirectedParser extends AbstractOutputParser {

    private final OutputParser backend;

    public RedirectedParser(OutputParser backend) {
        this.backend = backend;
    }

    @Override
    protected void doParse(String line) {
        this.backend.parse(line);
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
