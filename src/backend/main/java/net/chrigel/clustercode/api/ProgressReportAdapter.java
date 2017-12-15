package net.chrigel.clustercode.api;

import net.chrigel.clustercode.transcode.TranscodeProgress;

import java.util.function.Function;

public interface ProgressReportAdapter<R>
        extends Function<TranscodeProgress, R> {

    R getReportForInactiveEncoding();

    @Override
    R apply(TranscodeProgress output);

}
