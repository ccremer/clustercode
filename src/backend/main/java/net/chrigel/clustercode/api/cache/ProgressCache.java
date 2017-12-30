package net.chrigel.clustercode.api.cache;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegOutput;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeOutput;

import java.util.Optional;

@XSlf4j
public class ProgressCache {

    private FfmpegOutput ffmpegOutput;
    private HandbrakeOutput handbrakeOutput;
    @Getter
    private double percentage = -1;

    @Inject
    ProgressCache(RxEventBus eventBus) {

        eventBus.register(FfmpegOutput.class, this::onFfmpegOutputUpdated);
        eventBus.register(HandbrakeOutput.class, this::onHandbrakeOutputUpdated);

        eventBus.register(TranscodeFinishedEvent.class, this::onTranscodingFinished);
    }

    private void onTranscodingFinished(TranscodeFinishedEvent event) {
        log.entry(event);
        this.percentage = -1;
        this.ffmpegOutput = null;
        this.handbrakeOutput = null;
    }

    @Synchronized
    private void onHandbrakeOutputUpdated(HandbrakeOutput output) {
        log.entry(output);
        this.handbrakeOutput = output;
        this.percentage = output.getPercentage();
    }

    @Synchronized
    private void onFfmpegOutputUpdated(FfmpegOutput output) {
        log.entry(output);
        this.ffmpegOutput = output;
        this.percentage = output.getPercentage();
    }

    public Optional<FfmpegOutput> getFfmpegOutput() {
        return Optional.ofNullable(ffmpegOutput);
    }

    public Optional<HandbrakeOutput> getHandbrakeOutput() {
        return Optional.ofNullable(handbrakeOutput);
    }
}
