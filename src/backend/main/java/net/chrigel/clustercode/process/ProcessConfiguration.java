package net.chrigel.clustercode.process;

import io.reactivex.Observable;
import lombok.*;

import java.nio.file.Path;
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

    private Consumer<Observable<String>> errorObserver;

    private Consumer<Observable<String>> stdoutObserver;
}
