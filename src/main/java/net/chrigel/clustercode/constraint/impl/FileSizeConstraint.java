package net.chrigel.clustercode.constraint.impl;

import com.google.inject.name.Named;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.scan.ScanSettings;
import net.chrigel.clustercode.task.MediaCandidate;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This constraint checks the file size of the given argument. If the file is too big or too small it will be
 * rejected. The limits are configurable. If the minimum or maximum size are zero or negative, the check is disabled
 * (for its respective limit).
 */
@XSlf4j
public class FileSizeConstraint
        implements Constraint {

    public static long BYTES = 1;
    public static long KIBI_BYTES = BYTES * 1024;
    public static long MEBI_BYTES = KIBI_BYTES * 1024;
    private final long minSize;
    private final long maxSize;
    private final ScanSettings scanSettings;
    private final boolean enabled;

    @Inject
    FileSizeConstraint(@Named(ConstraintConfiguration.CONSTRAINT_FILE_MIN_SIZE_KEY) long minSize,
                       @Named(ConstraintConfiguration.CONSTRAINT_FILE_MAX_SIZE_KEY) long maxSize,
                       ScanSettings scanSettings) {
        this(minSize, maxSize, MEBI_BYTES, scanSettings);
    }

    protected FileSizeConstraint(long minSize,
                                 long maxSize,
                                 long factor,
                                 ScanSettings scanSettings) {
        this.scanSettings = scanSettings;
        this.enabled = (minSize > 0 || maxSize > 0);
        checkSizes(minSize, maxSize);
        this.minSize = minSize * factor;
        this.maxSize = maxSize * factor;
    }

    private void checkSizes(long minSize, long maxSize) {
        if (enabled && Math.min(minSize, maxSize) > 0 && minSize >= maxSize) {
            throw new IllegalArgumentException("minSize cannot be >= maxSize.");
        }
    }

    @Override
    public boolean accept(MediaCandidate candidate) {
        if (enabled) {
            Path file = scanSettings.getBaseInputDir().resolve(candidate.getSourcePath());
            try {
                long size = Files.size(file);
                if (minSize > 0 && maxSize > 0) {
                    // file between max and min
                    return size >= minSize && size <= maxSize;
                } else if (minSize <= 0) {
                    // size smaller than max, min disabled
                    return size <= maxSize;
                } else {
                    // size greater than min, max disabled
                    return size >= minSize;
                }
            } catch (IOException e) {
                log.warn("Could not determine file size of {}: {}. Ignoring file.", file, e.toString());
                return false;
            }
        } else {
            return true;
        }
    }

}
