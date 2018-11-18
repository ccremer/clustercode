package clustercode.api.rest.v1.hook;

import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.api.transcode.TranscodeReport;
import com.google.inject.Inject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j
public class ProgressHookImpl implements ProgressHook {

    private TranscodeReport latestProgressOutput;

    @Inject
    ProgressHookImpl(RxEventBus eventBus) {

        eventBus.listenFor(TranscodeReport.class, this::onProgressUpdated);

        eventBus.listenFor(TranscodeFinishedEvent.class, this::onTranscodingFinished);
    }

    @Synchronized
    private void onProgressUpdated(TranscodeReport output) {
        log.entry(output);
        this.latestProgressOutput = output;
    }

    @Synchronized
    private void onTranscodingFinished(TranscodeFinishedEvent event) {
        log.entry(event);
        this.latestProgressOutput = null;
    }

    @Override
    public double getPercentage() {
        if (latestProgressOutput == null) return -1d;
        return latestProgressOutput.getPercentage();
    }

}
