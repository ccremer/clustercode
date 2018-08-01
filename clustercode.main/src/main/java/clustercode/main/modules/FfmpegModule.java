package clustercode.main.modules;

import clustercode.api.transcode.OutputParser;
import clustercode.impl.transcode.parser.FfmpegParser;
import com.google.inject.AbstractModule;

public class FfmpegModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(OutputParser.class).to(FfmpegParser.class);
    }
}
