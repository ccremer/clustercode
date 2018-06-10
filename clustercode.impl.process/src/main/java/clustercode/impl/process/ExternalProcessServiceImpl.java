package clustercode.impl.process;

import clustercode.api.process.ExternalProcessService;
import clustercode.api.process.ProcessConfiguration;
import clustercode.api.process.RunningExternalProcess;
import clustercode.impl.util.Platform;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.XSlf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
            .fromCallable(() -> {
                assert Thread.currentThread().getName().startsWith("RxCachedThreadScheduler");
                ProcessBuilder builder = new ProcessBuilder(buildArguments(c));
                if (Platform.currentPlatform() == Platform.WINDOWS) {
                    // This is necessary. Otherwise waitFor() will be deadlocked even if the process finished hours ago.
                    builder.redirectErrorStream(true);
                }
                if (c.getWorkingDir() != null) builder.directory(c.getWorkingDir().toFile());

                log.info("Invoking: {}", builder.command());
                Process process = builder.start();

                if (Platform.currentPlatform() != Platform.WINDOWS) {
                    Optional.ofNullable(c.getErrorObserver())
                            .ifPresent(observer ->
                                captureOutput(observer, process.getErrorStream()));
                }
                Optional.ofNullable(c.getStdoutObserver())
                        .ifPresent(observer ->
                            captureOutput(observer, process.getInputStream()));

                if (processHandler != null) createHandle(processHandler, process);

                return process.waitFor();
            })
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .doOnError(ex -> log.warn(ex.toString()))
            .doAfterSuccess(exitCode ->
                log.info("Process finished with exit code {}: {}",
                    exitCode,
                    c.getExecutable()));
    }

    private void createHandle(Consumer<RunningExternalProcess> processHandler, Process process) {
        Single
            .just(new RunningProcessImpl(process))
            .observeOn(Schedulers.io())
            .subscribe(processHandler::accept);
    }

    private void captureOutput(Consumer<Observable<String>> observer, InputStream stream) {
        Subject<Object> subject = ReplaySubject.create().toSerialized();
        readStreamAsync(subject, stream);
        observer.accept(subject.ofType(String.class)
                               .observeOn(Schedulers.io()));
    }

    private void readStreamAsync(Subject<Object> replaySubject, InputStream stream) {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    replaySubject.onNext(line);
                }
                replaySubject.onComplete();
            } catch (IOException ex) {
                replaySubject.onError(ex);
            }
        });
    }

    private List<String> buildArguments(ProcessConfiguration c) {
        List<String> args = new LinkedList<>();
        args.add(c.getExecutable().toString());
        args.addAll(c.getArguments());
        return args;
    }

}
