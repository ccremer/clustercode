package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.transcode.impl.AbstractOutputParser;

@XSlf4j
public class FfprobeParser
        extends AbstractOutputParser {

    @Override
    protected void doParse(String line) {
        try {
            FfprobeOutput.builder()
                    .frameCount(Long.valueOf(line))
                    .build();
        } catch (NumberFormatException ex) {
            log.warn("Could not determine frame count.", ex);
        }
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }
}
