package net.chrigel.clustercode.transcode;

import java.nio.file.Path;

public interface TranscoderSettings {

    Path getTranscoderExecutable();

    boolean isIoRedirected();

}
