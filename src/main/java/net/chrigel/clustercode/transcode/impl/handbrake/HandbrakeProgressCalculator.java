package net.chrigel.clustercode.transcode.impl.handbrake;

import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.impl.ProgressCalculator;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class HandbrakeProgressCalculator
        implements ProgressCalculator {

    private AtomicBoolean enabled = new AtomicBoolean();
    private OutputParser<HandbrakeOutput> parser;

    @Inject
    HandbrakeProgressCalculator(OutputParser<HandbrakeOutput> parser){
        this.parser = parser;
    }

    @Override
    public void setTask(TranscodeTask task) {

    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        if (enabled) {
            parser.start();
        } else {
            parser.stop();
        }
    }

    @Override
    public Optional<? extends TranscodeProgress> getProgress() {
        if (!enabled.get() && !parser.getResult().isPresent()) return Optional.empty();
        return parser.getResult();
    }

    @Override
    public OutputParser<? extends TranscodeProgress> getParser() {
        return parser;
    }
}
