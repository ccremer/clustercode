package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.transcode.OutputParser;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegOutput;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegParser;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeOutput;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeParser;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum Transcoder implements EnumeratedImplementation {

    FFMPEG(FfmpegParser.class, FfmpegOutput.class),
    HANDBRAKE(HandbrakeParser.class, HandbrakeOutput.class);

    private final Class<? extends OutputParser> implementingClass;
    private final Class<? extends TranscodeProgress> outputType;

    Transcoder(Class<? extends OutputParser> implementingClass,
               Class<? extends TranscodeProgress> outputType) {
        this.implementingClass = implementingClass;
        this.outputType = outputType;
    }

    @Override
    public Class<? extends OutputParser> getImplementingClass() {
        return implementingClass;
    }

    public Class<? extends TranscodeProgress> getOutputType() {
        return outputType;
    }
}
