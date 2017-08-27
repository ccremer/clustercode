package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.impl.ProgressCalculator;
import net.chrigel.clustercode.util.Platform;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@XSlf4j
public class FfmpegProgressCalculator
        implements ProgressCalculator {

    private Long maxFrameCount = -1L;
    private OutputParser<FfprobeOutput> ffprobeParser;
    private Provider<ExternalProcess> externalProcessProvider;
    private MediaScanSettings mediaScanSettings;
    private OutputParser<FfmpegOutput> ffmpegParser;
    private AtomicBoolean enabled;

    @Inject
    FfmpegProgressCalculator(OutputParser<FfprobeOutput> ffprobeParser,
                             Provider<ExternalProcess> externalProcessProvider,
                             MediaScanSettings mediaScanSettings,
                             OutputParser<FfmpegOutput> ffmpegParser) {
        this.ffprobeParser = ffprobeParser;
        this.externalProcessProvider = externalProcessProvider;
        this.mediaScanSettings = mediaScanSettings;
        this.ffmpegParser = ffmpegParser;
        this.enabled = new AtomicBoolean();

    }

    void setMaxFrameCount(long count) {
        this.maxFrameCount = count;
    }

    void determineFrameCount(Path source) {
        if (Platform.currentPlatform() == Platform.WINDOWS) return;
        log.debug("Trying to get the total frame count...");
        ffprobeParser.start();
        val exitCode = externalProcessProvider.get()
                .withExecutablePath(Paths.get("/usr", "bin", "ffprobe"))
                .withIORedirected(false)
                .withArguments(Arrays.asList(
                        "-v", "error",
                        "-count_frames",
                        "-select_streams", "v:0",
                        "-show_entries", "stream=nb_read_frames",
                        "-of", "default=nokey=1:noprint_wrappers=1",
                        mediaScanSettings.getBaseInputDir().resolve(source).toString()))
                .withStdoutParser(ffprobeParser)
                .start();
        long frameCount = -1L;
        if (probingWasSuccessful(exitCode)) frameCount = ffprobeParser.getResult().get().getFrameCount();
        log.debug("Frames counted: {}", frameCount);
        setMaxFrameCount(frameCount);
        ffprobeParser.stop();
    }

    private boolean probingWasSuccessful(Optional<Integer> exitCode) {
        return exitCode.isPresent() && exitCode.get() == 0 && ffprobeParser.getResult().isPresent();
    }

    public Optional<? extends TranscodeProgress> getProgress() {
        if (!enabled.get() && !ffmpegParser.getResult().isPresent()) return Optional.empty();

        val out = ffmpegParser.getResult().get();

        out.setMaxFrame(maxFrameCount);
        out.setPercentage(calculatePercentage(out.getFrame()));
        return Optional.of(out);
    }

    @Synchronized
    @Override
    public void setTask(TranscodeTask task) {
        determineFrameCount(task.getMedia().getSourcePath());
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

    private double calculatePercentage(long framesSoFar) {
        if (maxFrameCount <= 0 || framesSoFar == 0) return -1d;
        return 100d / maxFrameCount * framesSoFar;
    }

    @Override
    public OutputParser<? extends TranscodeProgress> getParser() {
        return ffmpegParser;
    }

}
