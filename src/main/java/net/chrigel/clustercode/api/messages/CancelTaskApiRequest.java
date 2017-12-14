package net.chrigel.clustercode.api.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CancelTaskApiRequest implements ApiMessage {

    private String hostname;

}
