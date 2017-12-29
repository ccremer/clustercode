package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegParser;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeParser;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum Transcoder implements EnumeratedImplementation {

    FFMPEG(FfmpegParser.class),
    HANDBRAKE(HandbrakeParser.class);

    private final Class<? extends OutputParser> implementingClass;

    Transcoder(Class<? extends OutputParser> implementingClass) {
        this.implementingClass = implementingClass;
    }

    @Override
    public Class<? extends OutputParser> getImplementingClass() {
        return implementingClass;
    }
}
