package net.chrigel.clustercode.process;


import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Represents and wraps an external process. Provides a fluent-like API and throws runtime exceptions on unexpected
 * or configuration errors. Generally passing null is regarded as programmer error. The implementation is not
 * guaranteed to be thread-safe. For each subprocess to be started use separate instances. A subprocess can only be
 * launched once.
 */
public interface ExternalProcess {

    /**
     * Redirects the output and error stream. If given true, this JVM will print the output in the same stream as it
     * is bound currently. If false, any output will be omitted.
     *
     * @param redirectIO
     * @return this.
     */
    ExternalProcess withIORedirected(boolean redirectIO);

    ExternalProcess withStdoutParser(OutputParser stdParser);

    ExternalProcess withStderrParser(OutputParser errParser);

    /**
     * Appends additional arguments to the executable. Each entry will be string escaped if there is a whitespace in it.
     *
     * @param arguments the list of arguments. Can be empty.
     * @return this.
     */
    ExternalProcess withArguments(List<String> arguments);

    /**
     * Sets the path to the executable. This field is mandatory. In case of a script interpreter this field can be
     * set to an empty string (""), but this may platform dependant. The JVM needs the permissions to execute.
     *
     * @param path the (preferably) full path to the executable file.
     * @return this.
     */
    ExternalProcess withExecutablePath(Path path);

    /**
     * Sets the working directory for the subprocess. This functionality is platform dependant and may not work in
     * all cases. The default is the absolute path of this JVM.
     *
     * @param path the relative or absolute directory.
     * @return this.
     */
    ExternalProcess withCurrentWorkingDirectory(Path path);

    /**
     * Use this method to suppress the output of the implementation logs. Useful if the arguments contain sensitive
     * information. This does not prevent printing the output from the subprocess if
     * {@link #withIORedirected(boolean)} is enabled. On errors, only the exit code will be logged.
     *
     * @return this.
     */
    ExternalProcess withLogSuppressed();

    /**
     * Starts and awaits the process. This method blocks until the process terminated.
     *
     * @return an optional with the exit code. If there was an exception at running the process, the result will be
     * empty and logged as error.
     * @throws RuntimeException if the wrapper is configured incorrectly or this method is invoked multiple times.
     */
    Optional<Integer> start();

    /**
     * Starts the process in the background. Use either this method or {@link #start()}. Retrieve the result with {@link
     * RunningExternalProcess#waitFor()}.
     *
     * @return this.
     * @throws RuntimeException if the wrapper is configured incorrectly or this method is invoked multiple times.
     */
    RunningExternalProcess startInBackground();

}
