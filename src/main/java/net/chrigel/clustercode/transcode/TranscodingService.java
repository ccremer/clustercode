package net.chrigel.clustercode.transcode;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.Media;

import java.util.function.Consumer;

public interface TranscodingService {

    /**
     * Performs the transcoding. This method blocks until the process finished or failed.
     *
     * @param candidate
     * @param profile
     * @return
     */
    boolean transcode(Media candidate, Profile profile);

    /**
     * Starts the transcoding process in the background and notifies the provided listener upon completion (or
     * failure). A boolean will be passed to the listener, indicating whether transcoding was successful. Only one task
     * may be active at the same time, therefore calling this method twice has no effect.
     *
     * @param candidate the candidate, not null.
     * @param profile   the profile, not null.
     * @param listener  the listener instance.
     */
    void transcode(Media candidate, Profile profile, Consumer<Boolean> listener);

}
