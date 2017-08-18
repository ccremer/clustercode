package net.chrigel.clustercode.process;

import java.util.function.Consumer;

public interface OutputParser extends Consumer<String> {

    void start();

    void stop();

}
