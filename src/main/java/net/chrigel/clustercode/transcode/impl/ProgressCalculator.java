package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodeProgress;

import java.util.Optional;

public interface ProgressCalculator {

    void setTask(TranscodeTask task);

    void setEnabled(boolean enabled);

    OutputParser<? extends TranscodeProgress> getParser();
    Optional<? extends TranscodeProgress> getProgress();
}
