package net.chrigel.clustercode.cleanup;

import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;

public interface CleanupService {

    void performCleanup(TranscodeFinishedEvent result);

}
