package clustercode.api.domain;

import lombok.*;

@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class TranscodeResult {

    @Getter
    private final Media media;

    @Getter
    private final Profile profile;

}
