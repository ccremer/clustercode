package clustercode.main.modules;

import clustercode.api.transcode.ProgressParser;
import clustercode.impl.transcode.parser.FfmpegParser;
import com.google.inject.AbstractModule;

public class FfmpegModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProgressParser.class).to(FfmpegParser.class);
    }
}
