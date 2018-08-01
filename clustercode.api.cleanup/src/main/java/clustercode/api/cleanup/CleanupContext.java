package clustercode.api.cleanup;

import clustercode.api.event.messages.TranscodeFinishedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanupContext {

    private TranscodeFinishedEvent transcodeFinishedEvent;

    private Path outputPath;

}
