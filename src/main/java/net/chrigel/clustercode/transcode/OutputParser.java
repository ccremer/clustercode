package net.chrigel.clustercode.transcode;

import io.reactivex.Observable;

/**
 * Represents a parser that turns a line of string into a progress DTO.
 */
public interface OutputParser {

    /**
     * Parses the given non-null line.
     *
     * @param line the line.
     */
    void parse(String line);

    Observable<TranscodeProgress> onProgressParsed();

}
