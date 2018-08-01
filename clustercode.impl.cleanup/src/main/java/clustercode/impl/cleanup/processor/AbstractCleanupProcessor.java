package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupProcessor;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractCleanupProcessor implements CleanupProcessor {

    @Getter
    @Setter
    private int index;

    @Override
    public int compareTo(CleanupProcessor o) {
        return Integer.compare(getIndex(), o.getIndex());
    }

}
