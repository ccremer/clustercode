package net.chrigel.clustercode.process;

/**
 * Represents a parser that is capable of parsing string lines from a forked subprocess.
 */
public interface OutputParser {

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
     * Parses the given non-null line.
     *
     * @param line the line.
     */
    void parse(String line);

}
