package net.chrigel.clustercode.task;

import java.util.List;
import java.util.Optional;

public interface SelectionService {

    Optional<Media> selectMedia(List<Media> list);

}
