package net.chrigel.clustercode.scan.impl;

import com.google.inject.AbstractModule;
import net.chrigel.clustercode.scan.FileScanner;
import net.chrigel.clustercode.scan.ScanService;
import net.chrigel.clustercode.scan.ScanSettings;

public class ScanModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileScanner.class).to(FileScannerImpl.class);
        bind(ScanSettings.class).to(ScanSettingsImpl.class);
        bind(ScanService.class).to(ScanServiceImpl.class);
    }

}
