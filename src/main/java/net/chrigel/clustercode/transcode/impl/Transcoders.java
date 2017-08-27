package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegProgressCalculator;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeProgressCalculator;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum Transcoders implements EnumeratedImplementation {

    FFMPEG(FfmpegProgressCalculator.class),
    HANDBRAKE(HandbrakeProgressCalculator.class);

    private final Class<? extends ProgressCalculator> implementingClass;

    Transcoders(Class<? extends ProgressCalculator> implementingClass) {
        this.implementingClass = implementingClass;
    }

    @Override
    public Class<? extends ProgressCalculator> getImplementingClass() {
        return implementingClass;
    }
}
