package net.chrigel.clustercode.cleanup;

import net.chrigel.clustercode.transcode.TranscodeResult;

public interface CleanupService {

    void performCleanup(TranscodeResult result);

}
