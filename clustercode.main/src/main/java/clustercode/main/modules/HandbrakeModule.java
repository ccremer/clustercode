package clustercode.main.modules;

import clustercode.api.transcode.ProgressParser;
import clustercode.impl.transcode.parser.HandbrakeParser;
import com.google.inject.AbstractModule;

public class HandbrakeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProgressParser.class).to(HandbrakeParser.class);
    }
}
