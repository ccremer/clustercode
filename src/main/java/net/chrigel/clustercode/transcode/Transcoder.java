package net.chrigel.clustercode.transcode;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Transcoder {

    /**
     * Sets the source file.
     *
     * @param source the original source file with extension, not null.
     * @return this.
     */
    Transcoder from(Path source);

    /**
     * Sets the output file.
     *
     * @param output the output file with extension, not null.
     * @return this.
     */
    Transcoder to(Path output);

    /**
     * Sets the encoder options.
     *
     * @param arguments the options or arguments used by the encoder. Each entry may be a pair of 2 strings or a single
     *                  string. Not null and not used yet.
     * @return this.
     */
    Transcoder withArguments(Stream<String> arguments);

    /**
     * Launches the encoding process and waits for its termination.
     *
     * @return true if the conversion was successful, false otherwise (exit code != 0).
     */
     boolean transcode();

}
