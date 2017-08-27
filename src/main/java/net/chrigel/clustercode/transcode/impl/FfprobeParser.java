package net.chrigel.clustercode.transcode.impl;

import lombok.extern.slf4j.XSlf4j;

@XSlf4j
public class FfprobeParser
        extends AbstractOutputParser<FfprobeOutput> {

    @Override
    protected FfprobeOutput doParse(String line) {
        try {
            return FfprobeOutput.builder()
                    .frameCount(Long.valueOf(line))
                    .build();
        } catch (NumberFormatException ex) {
            log.warn("Could not determine frame count.", ex);
            return null;
        }
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }
}
