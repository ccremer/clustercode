package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
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

    private OutputParser<FfmpegOutput> ffmpegParser;
    private AtomicBoolean enabled;

    @Inject
    FfmpegProgressCalculator(OutputParser<FfmpegOutput> ffmpegParser) {
        this.ffmpegParser = ffmpegParser;
        this.enabled = new AtomicBoolean();

    }

    public Optional<? extends TranscodeProgress> getProgress() {
        if (!enabled.get() || !ffmpegParser.getResult().isPresent()) return Optional.empty();

        val out = ffmpegParser.getResult().get();

        out.setPercentage(calculatePercentage(out));
        return Optional.of(out);
    }

    @Synchronized
    @Override
    public void setTask(TranscodeTask task) {

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
