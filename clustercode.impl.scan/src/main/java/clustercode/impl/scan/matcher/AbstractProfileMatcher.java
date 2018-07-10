package clustercode.impl.scan.matcher;

import clustercode.api.scan.ProfileMatcher;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractProfileMatcher implements ProfileMatcher {

    @Override
    public int compareTo(ProfileMatcher o) {
        return Integer.compare(getIndex(), o.getIndex());
    }

    @Getter
    @Setter
    private int index;

}
