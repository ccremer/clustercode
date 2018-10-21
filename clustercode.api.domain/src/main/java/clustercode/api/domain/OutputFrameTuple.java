package clustercode.api.domain;

import lombok.Getter;

public class OutputFrameTuple {

    @Getter
    public final String line;
    @Getter
    public final OutputType type;

    public OutputFrameTuple(OutputType type, String line) {
        this.type = type;
        this.line = line;
    }

    public boolean isStdErrLine() {
        return this.type == OutputType.STDERR;
    }

    public boolean isStdOutLine() {
        return this.type == OutputType.STDOUT;
    }

    public enum OutputType {
        STDERR,
        STDOUT
    }

    public static OutputFrameTuple fromStdOut(String line) {
        return new OutputFrameTuple(OutputType.STDOUT, line);
    }

    public static OutputFrameTuple fromStdErr(String line) {
        return new OutputFrameTuple(OutputType.STDERR, line);
    }

    @Override
    public String toString() {
        return type.name() + ": " + line;
    }
}
