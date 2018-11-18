package clustercode.api.event.messages;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskAddedEvent {

    private UUID jobID;

    private int priority;

    private int sliceSize;

    @Builder.Default
    private List<String> args = new ArrayList<>();

}
