package clustercode.api.event.messages;

import lombok.*;

@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
public class StartupCompletedEvent {

    @Getter
    @NonNull
    private String mainVersion;

}
