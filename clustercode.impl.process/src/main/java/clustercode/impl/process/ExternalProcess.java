package clustercode.impl.process;

import clustercode.api.process.ProcessConfiguration;
import clustercode.api.process.RunningExternalProcess;
import clustercode.impl.util.Platform;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
class ExternalProcess {

    private final ProcessConfiguration c;
    private final Consumer<RunningExternalProcess> processHandler;

    private final Subject<Object> stdErrReplaySubject = PublishSubject.create().toSerialized();
    private final Subject<Object> stdOutReplaySubject = PublishSubject.create().toSerialized();

    private Process process;

    ExternalProcess(ProcessConfiguration c, Consumer<RunningExternalProcess> processHandler) {
        this.c = c;
        this.processHandler = processHandler;
    }

    public int start() throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder(buildArguments());
        if (Platform.currentPlatform() == Platform.WINDOWS) {
            // This is necessary. Otherwise waitFor() will be deadlocked even if the process finished hours ago.
            builder.redirectErrorStream(true);
        }
        if (c.getWorkingDir() != null) builder.directory(c.getWorkingDir().toFile());

        log.info("Invoking: {}", builder.command());
        this.process = builder.start();

        if (Platform.currentPlatform() != Platform.WINDOWS) {
            c.getErrorObservers().forEach(consumer ->
                    stdErrReplaySubject.ofType(String.class)
                                       .observeOn(Schedulers.computation())
                                       .subscribe(consumer::accept));
        }
        c.getStdoutObservers().forEach(consumer ->
                stdOutReplaySubject.ofType(String.class)
                                   .observeOn(Schedulers.computation())
                                   .subscribe(consumer::accept));

        readStreamAsync(process.getInputStream(), stdOutReplaySubject);
        readStreamAsync(process.getErrorStream(), stdErrReplaySubject);

        createHandle();

        return process.waitFor();
    }

    private List<String> buildArguments() {
        List<String> args = new LinkedList<>();
        args.add(c.getExecutable().toString());
        args.addAll(c.getArguments());
        return args;
    }

    private void readStreamAsync(InputStream stream, Subject<Object> subject) {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    subject.onNext(line);
                }
                subject.onComplete();
            } catch (IOException ex) {
                subject.onError(ex);
            }
        });
    }

    private void createHandle() {
        if (processHandler == null) return;
        Single.just(new RunningProcessImpl(process))
              .observeOn(Schedulers.io())
              .subscribe(processHandler::accept);
    }

}
