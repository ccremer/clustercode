package net.chrigel.clustercode.api.impl;

import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum ProgressReportAdapters
    implements EnumeratedImplementation<ProgressReportAdapter> {

    FFMPEG(FfmpegProgressAdapter.class),
    HANDBRAKE(HandbrakeProgressAdapter.class);

    private final Class<? extends ProgressReportAdapter>
        implementingClass;

    ProgressReportAdapters(
        Class<? extends ProgressReportAdapter>
            implementingClass) {
        this.implementingClass = implementingClass;
    }

    @Override
    public Class<? extends ProgressReportAdapter>
    getImplementingClass() {
        return implementingClass;
    }

}
