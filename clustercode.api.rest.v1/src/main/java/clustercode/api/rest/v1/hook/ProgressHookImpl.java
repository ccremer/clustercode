package clustercode.api.rest.v1.hook;

import clustercode.api.transcode.Transcoder;
import com.google.inject.Inject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import clustercode.api.rest.v1.ProgressReport;
import clustercode.api.rest.v1.ProgressReportAdapter;
import clustercode.api.event.RxEventBus;
import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.TranscoderConfig;
import clustercode.api.transcode.messages.TranscodeFinishedEvent;

@XSlf4j
public class ProgressHookImpl implements ProgressHook {

    private final ProgressReportAdapter progressAdapter;

    private final TranscoderConfig settings;
    private TranscodeProgress latestProgressOutput;

    @Inject
    ProgressHookImpl(RxEventBus eventBus,
                     ProgressReportAdapter progressAdapter,
                     TranscoderConfig settings) {
        this.progressAdapter = progressAdapter;
        this.settings = settings;

        eventBus.register(settings.getTranscoderType().getOutputType(), this::onProgressUpdated);

        eventBus.register(TranscodeFinishedEvent.class, this::onTranscodingFinished);
    }

    @Synchronized
    private void onProgressUpdated(TranscodeProgress output) {
        log.entry(output);
        this.latestProgressOutput = output;
    }

    @Synchronized
    private void onTranscodingFinished(TranscodeFinishedEvent event) {
        log.entry(event);
        this.latestProgressOutput = null;
    }

    @Override
    public ProgressReport getLatestProgressOutput() {
        if (latestProgressOutput == null) return progressAdapter.getReportForInactiveEncoding();
        return progressAdapter.apply(latestProgressOutput);
    }

    @Override
    public double getPercentage() {
        if (latestProgressOutput == null) return -1d;
        return latestProgressOutput.getPercentage();
    }

    /**
     * Gets the type of the locally configured transcoder.
     *
     * @return the enum type.
     */
    @Override
    public Transcoder getTranscoder() {
        return settings.getTranscoderType();
    }
}
