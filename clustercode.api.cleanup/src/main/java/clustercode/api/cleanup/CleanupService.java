package clustercode.api.cleanup;

import clustercode.api.event.messages.TranscodeFinishedEvent;

public interface CleanupService {

    void performCleanup(TranscodeFinishedEvent result);

}
