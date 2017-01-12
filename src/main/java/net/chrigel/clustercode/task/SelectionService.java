package net.chrigel.clustercode.task;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SelectionService {

    Optional<MediaCandidate> selectJob(List<MediaCandidate> list);

}
