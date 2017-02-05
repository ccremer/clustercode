package net.chrigel.clustercode.cleanup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.transcode.TranscodeResult;

import java.nio.file.Path;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanupContext {

    private TranscodeResult transcodeResult;

    private Path outputPath;

}
