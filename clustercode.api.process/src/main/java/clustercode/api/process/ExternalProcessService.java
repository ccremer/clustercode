package clustercode.api.process;

import io.reactivex.Single;

import java.util.function.Consumer;

public interface ExternalProcessService {

    Single<Integer> start(ProcessConfiguration configuration);

    Single<Integer> start(ProcessConfiguration configuration,
                          Consumer<RunningExternalProcess> processHandler);

}
