package clustercode.impl.process;

import clustercode.api.process.ExternalProcessService;
import clustercode.api.process.ProcessConfiguration;
import clustercode.api.process.RunningExternalProcess;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.XSlf4j;

import java.util.function.Consumer;

@XSlf4j
public class ExternalProcessServiceImpl implements ExternalProcessService {

    @Override
    public Single<Integer> start(ProcessConfiguration configuration) {
        return start(configuration, null);
    }

    @Override
    public Single<Integer> start(ProcessConfiguration c,
                                 Consumer<RunningExternalProcess> processHandler) {
        return Single
            .fromCallable(() -> new ExternalProcess(c, processHandler).start())
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .doOnError(ex -> log.warn(ex.toString()))
            .doAfterSuccess(exitCode ->
                log.info("Process finished with exit code {}: {}",
                    exitCode,
                    c.getExecutable()));
    }

}
