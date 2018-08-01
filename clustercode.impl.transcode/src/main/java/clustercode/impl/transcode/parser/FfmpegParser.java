package clustercode.impl.transcode.parser;

import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.output.FfmpegOutput;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@XSlf4j
public class FfmpegParser
    extends AbstractOutputParser {

    /*
    frame=\s*(\d+)\s*fps=\s*(\d*\.?\d*).*size=\s*(\d*)kB\s+time=(\d{2}:\d{2}:\d{2}).*bitrate=\s*(\d+\.?\d*)kbits\/s
    (?:\s?speed=)?(\d+\.?\d*)?
     */
    private static final Pattern progressPattern = Pattern.compile("frame=\\s*(\\d+)\\s*fps=\\s*(\\d*\\.?\\d*)" +
            ".*size=\\s*(\\d*)kB\\s+time=(\\d{2}:\\d{2}:\\d{2}).*bitrate=\\s*(\\d+\\.?\\d*)kbits\\/s(?:\\s?speed=)?" +
            "(\\d+\\.?\\d*)?");
    /*
    \s*Duration:\s*(\d+:\d{2}:\d{2}(?:\.\d{1,3})?)
     */
    private static final Pattern durationPattern = Pattern.compile("\\s*Duration:\\s*(\\d+:\\d{2}:\\d{2}(?:\\.\\d{1," +
            "3})?)");

    private boolean foundDuration = false;
    private final PublishSubject<TranscodeProgress> publishSubject = PublishSubject.create();

    @Override
    public Observable<TranscodeProgress> onProgressParsed() {
        return publishSubject
            .observeOn(Schedulers.computation())
            .ofType(TranscodeProgress.class);
    }

    @Override
    public void parse(String line) {
        // sample: frame=81624 fps= 33 q=-0.0 Lsize= 1197859kB time=00:56:44.38 bitrate=2882.4kbits/s speed=1.36x

        log.trace("Matching line: {}", line);

        val matcher = progressPattern.matcher(line);
        if (!matcher.find()) return;
        val frame = matcher.group(1);
        val fps = matcher.group(2);
        val size = matcher.group(3);
        val time = matcher.group(4);
        val bitrate = matcher.group(5);
        String speed = "0";
        if (matcher.groupCount() > 6) speed = matcher.group(6);

        FfmpegOutput result = new FfmpegOutput();
        if (!foundDuration) findDuration(result, line);

        result.setBitrate(getDoubleOrDefault(bitrate, 0d));
        result.setFps(getDoubleOrDefault(fps, 0d));
        result.setTime(parseDuration(time));
        result.setFileSize(calculateFileSize(size));
        result.setSpeed(getDoubleOrDefault(speed, 0d));
        result.setFrame(getLongOrDefault(frame, 0L));
        result.setPercentage(calculatePercentage(result));

        publishSubject.onNext(result);
    }

    private void findDuration(FfmpegOutput result, String line) {
        val matcher = durationPattern.matcher(line);

        if (!matcher.find()) return;
        val rawDuration = matcher.group(1);

        result.setDuration(parseDuration(rawDuration));
        log.debug("Duration: {}", result.getDuration());


        this.foundDuration = true;
    }

    Duration parseDuration(String rawDuration) {

        val arr = rawDuration.split(":");

        val input = "PT" + arr[0] + "H" + arr[1] + "M" + arr[2] + "S";
        try {
            return Duration.parse(input);
        } catch (DateTimeParseException ex) {
            return Duration.ofMillis(0);
        }
    }

    private double calculatePercentage(FfmpegOutput result) {

        val duration = result.getDuration().toMillis();
        val current = result.getTime().toMillis();

        if (current <= 0 || duration == 0) return 0d;
        return 100d / duration * current;
    }

    private double calculateFileSize(String value) {
        val raw = getDoubleOrDefault(value, 0d);
        if (Math.abs(raw) < 0.00000001d || raw == 0d) return 0d;
        return raw / 1024d;
    }
}
