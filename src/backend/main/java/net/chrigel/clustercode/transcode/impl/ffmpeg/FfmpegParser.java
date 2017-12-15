package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.transcode.impl.AbstractOutputParser;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@XSlf4j
public class FfmpegParser
        extends AbstractOutputParser<FfmpegOutput> {

    /*
    frame=\s*([0-9]+)\s*fps=\s*([0-9]*\.?[0-9]*).*size=\s*([0-9]*)kB\s+time=([0-9]{2}:[0-9]{2}:[0-9]{2}).*bitrate=\s*
    ([0-9]+\.?[0-9]*)kbits\/s(?:\s?speed=)?([0-9]+\.?[0-9]*)?
     */
    private static final Pattern progressPattern = Pattern.compile("frame=\\s*([0-9]+)\\s*fps=\\s*([0-9]*\\.?[0-9]*)" +
            ".*size=\\s*" +
            "([0-9]*)kB\\s+time=([0-9]{2}:[0-9]{2}:[0-9]{2}).*bitrate=\\s*([0-9]+\\.?[0-9]*)kbits\\/s(?:\\s?speed=)?" +
            "([0-9]+\\.?[0-9]*)?");
    /*
    \s*Duration:\s*(\d+:\d{2}:[0-9]{2}(?:\.\d{1,3})?)
     */
    private static final Pattern durationPattern = Pattern.compile("\\s*Duration:\\s*(\\d+:\\d{2}:[0-9]{2}(?:\\" +
            ".\\d{1,3})?)");

    private boolean foundDuration = false;
    private FfmpegOutput result = new FfmpegOutput();

    @Override
    public FfmpegOutput doParse(String line) {
        // sample: frame=81624 fps= 33 q=-0.0 Lsize= 1197859kB time=00:56:44.38 bitrate=2882.4kbits/s speed=1.36x

        log.trace("Matching line: {}", line);

        if (!foundDuration) findDuration(line);
        val matcher = progressPattern.matcher(line);
        if (!matcher.find()) return null;
        val frame = matcher.group(1);
        val fps = matcher.group(2);
        val size = matcher.group(3);
        val time = matcher.group(4);
        val bitrate = matcher.group(5);
        String speed = "0";
        if (matcher.groupCount() > 6) speed = matcher.group(6);


        result.setBitrate(getDoubleOrDefault(bitrate, 0d));
        result.setFps(getDoubleOrDefault(fps, 0d));
        result.setTime(parseDuration(time));
        result.setFileSize(calculateFileSize(size));
        result.setSpeed(getDoubleOrDefault(speed, 0d));
        result.setFrame(getLongOrDefault(frame, 0L));
        return result;
    }

    private void findDuration(String line) {
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

    @Override
    protected void doStart() {
        log.debug("Parsing from process output...");
    }

    @Override
    protected void doStop() {
        log.debug("Stopping parser.");
        foundDuration = false;
    }

    private double calculateFileSize(String value) {
        val raw = getDoubleOrDefault(value, 0d);
        if (Math.abs(raw) < 0.00000001d || raw == 0d) return 0d;
        return raw / 1024d;
    }
}
