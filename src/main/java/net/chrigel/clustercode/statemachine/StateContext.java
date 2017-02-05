package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.MediaScanSettings;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents a state context.
 */
public interface StateContext {

    /**
     * Gets the collection which holds the priority dir and their media candidates within it. Each sourcePath is relative to
     * {@link MediaScanSettings#getBaseInputDir()}
     *
     * @return the (empty) map.
     */
    Map<Path, List<Media>> getCandidates();

    /**
     * Sets the collection of media candidates. Each key has the potential media candidates, though the list itself
     * might be empty.
     *
     * @param pathListMap the map of the directories. Empty map if no input directories present.
     */
    void setCandidates(Map<Path, List<Media>> pathListMap);

}
