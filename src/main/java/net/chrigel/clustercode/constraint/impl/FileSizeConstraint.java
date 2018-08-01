package net.chrigel.clustercode.constraint.impl;

import com.google.inject.name.Named;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.InvalidConfigurationException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

/**
 * This constraint checks the file size of the given argument. If the file is too big or too small it will be
 * rejected. The limits are configurable. If the minimum or maximum size are 0 (zero), the verifyIn is disabled
 * (for its respective limit).
 */
class FileSizeConstraint
        extends AbstractConstraint {

    public static long BYTES = 1;
    public static long KIBI_BYTES = BYTES * 1024;
    public static long MEBI_BYTES = KIBI_BYTES * 1024;
    private final double minSize;
    private final double maxSize;
    private final MediaScanSettings scanSettings;
    private final DecimalFormat formatter = new DecimalFormat("#.####");

    @Inject
    FileSizeConstraint(@Named(ConstraintModule.CONSTRAINT_FILESIZE_MIN_SIZE_KEY) double minSize,
                       @Named(ConstraintModule.CONSTRAINT_FILESIZE_MAX_SIZE_KEY) double maxSize,
                       MediaScanSettings scanSettings) {
        this(minSize, maxSize, MEBI_BYTES, scanSettings);
    }

    protected FileSizeConstraint(double minSize,
                                 double maxSize,
                                 long factor,
                                 MediaScanSettings scanSettings) {
        checkConfiguration(minSize, maxSize);
        this.scanSettings = scanSettings;
        this.minSize = minSize * factor;
        this.maxSize = maxSize * factor;
    }

    private void checkConfiguration(double minSize, double maxSize) {
        if (Math.min(minSize, maxSize) > 0 && minSize > maxSize) {
            throw new InvalidConfigurationException("minSize cannot be greater than maxSize. Min: {}, Max: {}",
                    minSize, maxSize);
        }
        if (Math.min(minSize, maxSize) < 0) {
            throw new InvalidConfigurationException("File sizes cannot contain negative values. Min: {}, Max: {}",
                    minSize, maxSize);
        }
        if (minSize == maxSize) {
            throw new InvalidConfigurationException("File sizes cannot be equal. Min: {}, Max: {}",
                    minSize, maxSize);
        }
    }

    @Override
    public boolean accept(Media candidate) {
        Path file = scanSettings.getBaseInputDir().resolve(candidate.getSourcePath());
        try {
            long size = Files.size(file);
            if (minSize > 0 && maxSize > 0) {
                // file between max and min
                return logAndReturn(size >= minSize && size <= maxSize, file, size);
            } else if (minSize <= 0) {
                // size smaller than max, min disabled
                return logAndReturn(size <= maxSize, file, size);
            } else {
                // size greater than min, max disabled
                return logAndReturn(size >= minSize, file, size);
            }
        } catch (IOException e) {
            log.warn("Could not determine file size of {}: {}. Declined file.", file, e.toString());
            return false;
        }
    }

    protected String formatNumber(double number) {
        return formatter.format(number);
    }

    protected boolean logAndReturn(boolean result, Path file, long size) {
        return logAndReturnResult(
                result,
                "file size of {} with {} MB (min: {}, max: {})",
                file,
                formatNumber(size / MEBI_BYTES),
                formatNumber(minSize / MEBI_BYTES),
                formatNumber(maxSize / MEBI_BYTES));
    }
}

