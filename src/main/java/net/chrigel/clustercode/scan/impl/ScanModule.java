package net.chrigel.clustercode.scan.impl;

import com.google.inject.AbstractModule;
import net.chrigel.clustercode.scan.*;
import net.chrigel.clustercode.scan.impl.matcher.MostAppropriateMatcherStrategy;

public class ScanModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileScanner.class).to(FileScannerImpl.class);
        bind(MediaScanSettings.class).to(MediaScanSettingsImpl.class);
        bind(MediaScanService.class).to(MediaScanServiceImpl.class);

        bind(ProfileScanService.class).to(ProfileScanServiceImpl.class);
        bind(ProfileParser.class).to(ProfileParserImpl.class);

        bind(ProfileMatcherStrategy.class).to(MostAppropriateMatcherStrategy.class);
    }

}
