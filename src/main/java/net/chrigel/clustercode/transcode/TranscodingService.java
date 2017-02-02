package net.chrigel.clustercode.transcode;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.Media;

import java.util.function.Consumer;

public interface TranscodingService {

    /**
     * Performs the transcoding. This method blocks until the process finished or failed.
     *
     * @param candidate the task, not null.
     * @param profile   the profile, not null. The {@link Profile#getTemporaryFile()} field will return the file
     *                  written during transcoding. The file will NOT be moved to a different location.
     * @return true if transcoding was successful.
     */
    boolean transcode(Media candidate, Profile profile);

    /**
     * Runs {@link #transcode(Media, Profile)} in background.
     *
     * @param candidate the task, not null.
     * @param profile   the profile, not null.
     * @param listener  the listener instance for retrieving the result.
     */
    void transcode(Media candidate, Profile profile, Consumer<Boolean> listener);

}
