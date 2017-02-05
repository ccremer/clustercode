package net.chrigel.clustercode.scan;

import java.util.List;
import java.util.Optional;

public interface SelectionService {

    Optional<Media> selectMedia(List<Media> list);

}
