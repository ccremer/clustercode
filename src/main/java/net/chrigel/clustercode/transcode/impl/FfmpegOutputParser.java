package net.chrigel.clustercode.transcode.impl;

import lombok.extern.slf4j.XSlf4j;
import lombok.val;

import java.util.regex.Pattern;

@XSlf4j
public class FfmpegOutputParser
        extends AbstractOutputParser<FfmpegOutput> {

    /*
    frame=\s*([0-9]+)\s*fps=\s*([0-9]*\.?[0-9]*).*size=\s*([0-9]*)kB\s+time=([0-9]{2}:[0-9]{2}:[0-9]{2}).*bitrate=\s*
    ([0-9]+\.?[0-9]*)kbits\/s(?:\s?speed=)?([0-9]+\.?[0-9]*)?
     */
    private static Pattern pattern = Pattern.compile("frame=\\s*([0-9]+)\\s*fps=\\s*([0-9]*\\.?[0-9]*).*size=\\s*" +
            "([0-9]*)kB\\s+time=([0-9]{2}:[0-9]{2}:[0-9]{2}).*bitrate=\\s*([0-9]+\\.?[0-9]*)kbits\\/s(?:\\s?speed=)?" +
            "([0-9]+\\.?[0-9]*)?");
    private FfmpegOutput result = new FfmpegOutput();

    @Override
    public FfmpegOutput doParse(String line) {
        // sample: frame=81624 fps= 33 q=-0.0 Lsize= 1197859kB time=00:56:44.38 bitrate=2882.4kbits/s speed=1.36x
        //log.debug("Matching line: {}", line);

        val matcher = pattern.matcher(line);
        if (!matcher.find()) return null;
        val frame = matcher.group(1);
        val fps = matcher.group(2);
        val size = matcher.group(3);
        val time = matcher.group(4);
        val bitrate = matcher.group(5);
        String speed = "0";
        if (matcher.groupCount() > 6) speed = matcher.group(6);

        result.setBitrate(bitrate);
        result.setFps(fps);
        result.setTime(time);
        result.setSize(size);
        result.setSpeed(speed);
        result.setFrame(frame);
        return result;
    }

    @Override
    protected void doStart() {
        log.debug("Parsing from process output...");
    }

    @Override
    protected void doStop() {
        log.debug("Stopping parser.");
    }

}
