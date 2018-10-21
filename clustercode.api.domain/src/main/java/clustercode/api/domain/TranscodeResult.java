package clustercode.api.domain;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class TranscodeResult {

    @Getter
    @ToString.Exclude
    @NonNull
    private final List<OutputFrameTuple> frames;

    @Getter(lazy = true)
    @ToString.Exclude
    private final List<String> stderrLines = getLinesOfType(OutputFrameTuple.OutputType.STDERR);

    @Getter(lazy = true)
    @ToString.Exclude
    private final List<String> stdoutLines = getLinesOfType(OutputFrameTuple.OutputType.STDOUT);

    @Getter
    private final Media media;

    @Getter
    private final Profile profile;

    private List<String> getLinesOfType(OutputFrameTuple.OutputType type) {
        return frames.stream()
                     .filter(t -> t.type == type)
                     .map(t -> t.line)
                     .collect(Collectors.toList());
    }

}
