package net.chrigel.clustercode.transcode;

import net.chrigel.clustercode.transcode.impl.Transcoder;

import java.nio.file.Path;

public interface TranscoderSettings {

    /**
     * Gets the path to the transcoder executable.
     *
     * @return the path to the executable, not null.
     */
    Path getTranscoderExecutable();

    /**
     * Indicates whether the output of the subprocess should be included in this process' output stream (true) or not.
     *
     * @return
     */
    boolean isIoRedirected();

    /**
     * Gets the path to the temporary directory, which is needed during transcoding.
     *
     * @return the path to the dir.
     */
    Path getTemporaryDir();

    /**
     * Gets the default video extension with leading "." (e.g. ".mkv").
     *
     * @return the default extension, not null.
     */
    String getDefaultVideoExtension();

    /**
     * Gets the type of transcoder.
     *
     * @return the enum.
     */
    Transcoder getTranscoderType();

}
