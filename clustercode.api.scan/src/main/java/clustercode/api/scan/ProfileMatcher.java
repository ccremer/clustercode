package clustercode.api.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.impl.util.OptionalFunction;

public interface ProfileMatcher extends OptionalFunction<Media, Profile> {

}
