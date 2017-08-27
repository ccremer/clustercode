package net.chrigel.clustercode.transcode.impl;

import lombok.val;
import net.chrigel.clustercode.transcode.TranscodeProgress;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

class ProgressCalculator implements Consumer<FfmpegOutput> {

    private Long frameCount = -1L;
    private AtomicReference<FfmpegOutput> output;

    ProgressCalculator() {
        this.output = new AtomicReference<>();
    }

    void setFrameCount(long count) {
        this.frameCount = count;
    }

    Optional<TranscodeProgress> getProgress() {

        val out = this.output.get();
        if (out == null) return Optional.empty();

        val framesSoFar = getLongOrDefault(out.getFrame(), 0L);
        val progress = TranscodeProgress.builder()
                .bitrate(getDoubleOrDefault(out.getBitrate(), 0d))
                .fileSize(calculateFileSize(out.getSize()))
                .fps(getDoubleOrDefault(out.getFps(), 0d))
                .frame(framesSoFar)
                .speed(getDoubleOrDefault(out.getSpeed(), 0d))
                .maxFrame(frameCount)
                .percentage(calculatePercentage(framesSoFar))
                .build();

        return Optional.of(progress);
    }

    private double calculatePercentage(long framesSoFar) {
        if (frameCount <= 0 || framesSoFar == 0) return -1d;
        return 100d / frameCount * framesSoFar;
    }

    @Override
    public void accept(FfmpegOutput ffmpegOutput) {
        this.output.set(ffmpegOutput);
    }

    private double getDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private long getLongOrDefault(String value, long defaultValue) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private double calculateFileSize(String value) {
        val raw = getDoubleOrDefault(value, 0d);
        if (Math.abs(raw) < 0.00000001d || raw == 0d) return 0d;
        return raw / 1024d;
    }
}
