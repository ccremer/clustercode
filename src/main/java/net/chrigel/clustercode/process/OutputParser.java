package net.chrigel.clustercode.process;

import net.chrigel.clustercode.util.Publisher;

import java.util.Optional;

/**
 * Represents a parser that is capable of parsing string lines from a forked subprocess.
 *
 * @param <T> the type of the parsing result.
 */
public interface OutputParser<T> extends Publisher<T> {

    /**
     * Starts parsing the output. Does nothing if already started.
     */
    void start();

    /**
     * Stops parsing the output. Does nothing if already stopped.
     */
    void stop();

    /**
     * Returns true if the parser is started, otherwise false.
     */
    boolean isStarted();

    /**
     * Gets the current result of the parser. If the parser has run but is stopped now, it will return the last result.
     *
     * @return an instance of T. Returns an empty Optional if the parser is currently inactive or has not parsed
     * anything.
     */
    Optional<T> getResult();

    /**
     * Parses the given non-null line. This method should update its internal data structure, so that {@link
     * #getResult()} returns a value upon successful parsing.
     *
     * @param line the line.
     */
    void parse(String line);

}
