package net.chrigel.clustercode.transcode.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.process.OutputParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XSlf4j
public class FfmpegConsoleParser implements OutputParser {

    /*
    frame=\s*([0-9]+)\s*fps=\s*([0-9]*\.?[0-9]*).*size=\s*([0-9]*)kB\s+time=([0-9]{2}:[0-9]{2}:[0-9]{2}).*bitrate=\s*
    ([0-9]+\.?[0-9]*)kbits\/s(?:\s?speed=)?([0-9]+\.?[0-9]*)?
     */
    private static Pattern pattern = Pattern.compile("frame=\\s*([0-9]+)\\s*fps=\\s*([0-9]*\\.?[0-9]*).*size=\\s*" +
            "([0-9]*)kB\\s+time=([0-9]{2}:[0-9]{2}:[0-9]{2}).*bitrate=\\s*([0-9]+\\.?[0-9]*)kbits\\/s(?:\\s?speed=)?" +
            "([0-9]+\\.?[0-9]*)?");
    private boolean started;


    @Override
    public void accept(String line) {
        // sample: frame=81624 fps= 33 q=-0.0 Lsize= 1197859kB time=00:56:44.38 bitrate=2882.4kbits/s speed=1.36x
        //log.debug("Matching line: {}", line);

        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String frame = matcher.group(1);
            String fps = matcher.group(2);
            String size = matcher.group(3);
            String time = matcher.group(4);
            String bitrate = matcher.group(5);
            String speed = "0";
            if (matcher.groupCount() > 6) speed = matcher.group(6);

            StringBuilder builder = new StringBuilder("\r")
                    .append("frame: ").append(frame)
                    .append(", fps: ").append(fps)
                    .append(", size: ").append(size)
                    .append(", time: ").append(time)
                    .append(", bitrate: ").append(bitrate)
                    .append(", speed: ").append(speed);
            printToConsole(builder.toString());
        }
    }

    @Synchronized
    private void printToConsole(String line) {
        System.out.print(line);
    }

    @Synchronized
    @Override
    public void start() {
        if (started) return;
        this.started = true;
        log.debug("Parsing from process output to console...");
    }

    @Synchronized
    @Override
    public void stop() {
        if (!started) return;
        System.out.println();
        log.debug("Stopping parser.");
    }
}
