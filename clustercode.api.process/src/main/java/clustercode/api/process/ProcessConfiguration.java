package clustercode.api.process;

import lombok.*;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Data
@Builder
@AllArgsConstructor
public class ProcessConfiguration {

    @NonNull
    private Path executable;

    private Path workingDir;

    @Singular
    private List<String> arguments;

    @Singular
    private List<Consumer<String>> errorObservers = new LinkedList<>();

    @Singular
    private List<Consumer<String>> stdoutObservers = new LinkedList<>();
}
