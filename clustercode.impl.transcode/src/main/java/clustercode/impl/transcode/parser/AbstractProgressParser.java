package clustercode.impl.transcode.parser;

import clustercode.api.domain.OutputFrameTuple;
import clustercode.api.transcode.ProgressParser;
import clustercode.api.transcode.TranscodeReport;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.var;

import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractProgressParser implements ProgressParser {

    private final Subject<Object> publishSubject = PublishSubject.create().toSerialized();

    @Override
    public Stream<TranscodeReport> onProgressParsed() {

        var iter = publishSubject.ofType(TranscodeReport.class).blockingNext().iterator();
        return Stream.generate(iter::next);

    }

    @Override
    public void close() {
        publishSubject.onComplete();
    }

    @Override
    public ProgressParser onProgressParsed(Consumer<TranscodeReport> listener) {
        publishSubject.ofType(TranscodeReport.class)
                      .subscribe(listener::accept);
        return this;
    }

    @Override
    public final boolean doesNotMatchProgressLine(OutputFrameTuple line) {
        return !matchesProgressLine(line);
    }

    protected final double getDoubleOrDefault(String value, double defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected final long getLongOrDefault(String value, long defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

}
