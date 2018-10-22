package clustercode.impl.transcode.parser;

import clustercode.api.domain.OutputFrameTuple;
import clustercode.api.transcode.TranscodeReport;
import clustercode.api.transcode.output.HandbrakeOutput;
import lombok.extern.slf4j.XSlf4j;
import lombok.var;

import java.util.Optional;
import java.util.regex.Pattern;

@XSlf4j
public class HandbrakeParser extends AbstractProgressParser {

    /*
    Encoding:.*,\s*([0-9]+\.?[0-9]*)\s*%?(?:\s*\(([0-9]*\.?[0-9]*)?\s*fps,\s*avg\s*([0-9]+\.?[0-9]*)\s*fps,\s*ETA\s*
    ([0-9]+h[0-9]{2}m[0-9]{2})s\))?
     */
    private static Pattern pattern = Pattern.compile("Encoding:.*,\\s*([0-9]+\\.?[0-9]*)\\s*%?(?:\\s*\\(([0-9]*\\" +
            ".?[0-9]*)?\\s*fps,\\s*avg\\s*([0-9]+\\.?[0-9]*)\\s*fps,\\s*ETA\\s*([0-9]+h[0-9]{2}m[0-9]{2})s\\))?");

    @Override
    protected Optional<TranscodeReport> parseLine(String line) {

        log.trace("Parsing line: {}", line);
        var matcher = pattern.matcher(line);
        if (!matcher.find()) return Optional.empty();
        var percentage = matcher.group(1);
        var fps = matcher.group(2);
        var averageFps = matcher.group(3);
        var eta = matcher.group(4);

        return Optional.of(HandbrakeOutput
                .builder()
                .fps(getDoubleOrDefault(fps, 0d))
                .eta(eta == null
                        ? "00:00:00"
                        : eta.replace('h', ':')
                             .replace('m', ':'))
                .averageFps(getDoubleOrDefault(averageFps, 0d))
                .percentage(getDoubleOrDefault(percentage, 0d))
                .build());
    }

    @Override
    public boolean matchesProgressLine(OutputFrameTuple tuple) {
        // sample: Encoding: task 1 of 1, 5.11 % (67.61 fps, avg 67.59 fps, ETA 00h20m43s))

        var matcher = pattern.matcher(tuple.getLine());
        return matcher.find();
    }
}
