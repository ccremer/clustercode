package net.chrigel.clustercode.cleanup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;

import java.nio.file.Path;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanupContext {

    private TranscodeFinishedEvent transcodeFinishedEvent;

    private Path outputPath;

}
