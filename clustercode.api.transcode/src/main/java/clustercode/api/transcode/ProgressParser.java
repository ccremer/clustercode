package clustercode.api.transcode;

import clustercode.api.domain.OutputFrameTuple;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a parser that turns a line of string into a progress DTO.
 */
public interface ProgressParser {

    /**
     * Parses the given non-null line.
     *
     * @param line the line.
     */
    void parse(String line);

    /**
     * Adds a listener which gets notified on progress updates.
     *
     * @param listener
     * @return
     */
    ProgressParser onProgressParsed(Consumer<TranscodeReport> listener);

    Stream<TranscodeReport> onProgressParsed();

    /**
     * Returns true if the given tuple matches an expected progress report.
     *
     * @param line
     * @return
     */
    boolean matchesProgressLine(OutputFrameTuple line);

    /**
     * Negation of {@link #matchesProgressLine(OutputFrameTuple)}.
     *
     * @param line
     * @return
     */
    boolean doesNotMatchProgressLine(OutputFrameTuple line);

    void close();

}
