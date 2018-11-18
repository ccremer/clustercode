package clustercode.api.event.messages;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskCompletedEvent {

    private UUID jobID;

}
