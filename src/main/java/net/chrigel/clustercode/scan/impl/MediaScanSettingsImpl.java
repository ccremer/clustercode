package net.chrigel.clustercode.scan.impl;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.ToString;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.FilesystemProvider;
import net.chrigel.clustercode.util.InvalidConfigurationException;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@ToString
@Getter
public class MediaScanSettingsImpl implements MediaScanSettings {

    private String skipExtension;
    private final Path baseInputDir;
    private List<String> allowedExtensions;
    private long mediaScanInterval = 30;

    @Inject
    MediaScanSettingsImpl(
        @Named(ScanModule.MEDIA_INPUT_DIR_KEY) String baseDir
    ) {
        this.baseInputDir = FilesystemProvider.getInstance().getPath(baseDir);
        setAllowedExtensions("mkv,mp4,avi");
        setMediaScanInterval(mediaScanInterval);
    }

    private void checkInterval(long scanInterval) {
        if (scanInterval < 1) {
            throw new InvalidConfigurationException("The scan interval must be >= 1.");
        }
    }

    @Inject(optional = true)
    void setSkipExtension(@Named(ScanModule.MEDIA_SKIP_NAME_KEY) String skipExtension) {
        this.skipExtension = skipExtension;
    }

    @Inject(optional = true)
    void setMediaScanInterval(@Named(ScanModule.MEDIA_SCAN_INTERVAL_KEY) long mediaScanInterval) {
        checkInterval(mediaScanInterval);
        this.mediaScanInterval = mediaScanInterval;
    }

    @Inject(optional = true)
    void setAllowedExtensions(@Named(ScanModule.MEDIA_EXTENSIONS_KEY) String allowedExtensions) {
        this.allowedExtensions = new LinkedList<>(Arrays.asList(allowedExtensions.split(",")));
    }

}
