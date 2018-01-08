package net.chrigel.clustercode.api.hook;

import com.google.inject.Inject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.ProgressReport;
import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.impl.Transcoder;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;

@XSlf4j
public class ProgressHookImpl implements ProgressHook {

    private final ProgressReportAdapter progressAdapter;

    private final TranscoderSettings settings;
    private TranscodeProgress latestProgressOutput;

    @Inject
    ProgressHookImpl(RxEventBus eventBus,
                     ProgressReportAdapter progressAdapter,
                     TranscoderSettings settings) {
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
