package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.val;
import net.chrigel.clustercode.transcode.impl.AbstractOutputParser;

import javax.inject.Inject;

public class FfmpegConsoleParser
        extends AbstractOutputParser<String> {

    private final FfmpegOutputParser outputParser;

    @Inject
    FfmpegConsoleParser(FfmpegOutputParser outputParser) {
        this.outputParser = outputParser;
    }

    @Override
    public String doParse(String line) {
        outputParser.parse(line);
        val output = outputParser.getResult();
        return output.map(this::convertToString).orElse(null);
    }

    @Override
    protected void doStart() {
        outputParser.start();
    }

    @Override
    protected void doStop() {
        if (isStarted()) System.out.println();
        outputParser.stop();
    }

    private String convertToString(FfmpegOutput f) {
        val builder = new StringBuilder("\r")
                .append("frame: ").append(f.getFrame())
                .append(", fps: ").append(f.getFps())
                .append(", size: ").append(f.getFileSize())
                .append(", time: ").append(f.getTime())
                .append(", bitrate: ").append(f.getBitrate())
                .append(", speed: ").append(f.getSpeed());
        return builder.toString();
    }

}
