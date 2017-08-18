package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.OutputParser;

class NullParser implements OutputParser {

    @Override
    public void accept(String s) {
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
