package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.impl.ProgressCalculator;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@XSlf4j
public class FfmpegProgressCalculator
        implements ProgressCalculator {

    private final OutputParser<FfmpegOutput> ffmpegParser;
    private final ClusterService clusterService;
    private final AtomicBoolean enabled;
    private TranscodeTask currentTask;

    @Inject
    FfmpegProgressCalculator(OutputParser<FfmpegOutput> ffmpegParser,
                             ClusterService clusterService) {
        this.ffmpegParser = ffmpegParser;
        this.clusterService = clusterService;
        this.enabled = new AtomicBoolean();

    }

    @Override
    public Optional<? extends TranscodeProgress> getProgress() {
        if (!enabled.get() || !ffmpegParser.getResult().isPresent()) return Optional.empty();

        val out = ffmpegParser.getResult().get();

        out.setPercentage(calculatePercentage(out));
        return Optional.of(out);
    }

    @Synchronized
    @Override
    public void setTask(TranscodeTask task) {
        this.currentTask = task;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        if (enabled) {
            ffmpegParser.start();
        } else {
            ffmpegParser.stop();
        }
    }

    private double calculatePercentage(FfmpegOutput out) {

        val duration = out.getDuration().toMillis();
        val current = out.getTime().toMillis();

        if (current <= 0 || duration == 0) return 0d;
        return 100d / duration * current;
    }

    @Override
    public OutputParser<? extends TranscodeProgress> getParser() {
        return ffmpegParser;
    }

}
