package net.chrigel.clustercode.transcode.impl.handbrake;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.impl.AbstractOutputParser;

import java.util.regex.Pattern;

@XSlf4j
public class HandbrakeParser
    extends AbstractOutputParser {

    /*
    Encoding:.*,\s*([0-9]+\.?[0-9]*)\s*%?(?:\s*\(([0-9]*\.?[0-9]*)?\s*fps,\s*avg\s*([0-9]+\.?[0-9]*)\s*fps,\s*ETA\s*
    ([0-9]+h[0-9]{2}m[0-9]{2})s\))?
     */
    private static Pattern pattern = Pattern.compile("Encoding:.*,\\s*([0-9]+\\.?[0-9]*)\\s*%?(?:\\s*\\(([0-9]*\\" +
        ".?[0-9]*)?\\s*fps,\\s*avg\\s*([0-9]+\\.?[0-9]*)\\s*fps,\\s*ETA\\s*([0-9]+h[0-9]{2}m[0-9]{2})s\\))?");

    private Subject<Object> publishSubject = PublishSubject.create().toSerialized();

    @Override
    public void parse(String line) {
        // sample: Encoding: task 1 of 1, 5.11 % (67.61 fps, avg 67.59 fps, ETA 00h20m43s))

        log.trace("Matching line: {}", line);
        val matcher = pattern.matcher(line);
        if (!matcher.find()) return;
        val percentage = matcher.group(1);
        val fps = matcher.group(2);
        val averageFps = matcher.group(3);
        val eta = matcher.group(4);

        HandbrakeOutput result = new HandbrakeOutput();

        result.setFps(getDoubleOrDefault(fps, 0d));
        result.setEta(eta == null
            ? "00:00:00"
            : eta.replace('h', ':')
                 .replace('m', ':'));
        result.setAverageFps(getDoubleOrDefault(averageFps, 0d));
        result.setPercentage(getDoubleOrDefault(percentage, 0d));

        publishSubject.onNext(result);
    }

    @Override
    public Observable<TranscodeProgress> onProgressParsed() {
        return publishSubject
            .observeOn(Schedulers.computation())
            .ofType(TranscodeProgress.class);
    }
}
