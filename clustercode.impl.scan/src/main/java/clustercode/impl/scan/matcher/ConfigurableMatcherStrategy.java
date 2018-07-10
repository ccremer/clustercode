package clustercode.impl.scan.matcher;

import clustercode.api.scan.ProfileMatcher;
import clustercode.api.scan.ProfileMatcherStrategy;
import clustercode.impl.scan.ProfileScanConfig;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurableMatcherStrategy implements ProfileMatcherStrategy {

    private final List<ProfileMatcher> matchers;

    @Inject
    ConfigurableMatcherStrategy(ProfileScanConfig config,
                                Set<ProfileMatcher> matchers) {
        List<Class> classList = config.profile_matchers()
                                      .stream()
                                      .map(ProfileMatchers::getImplementingClass)
                                      .collect(Collectors.toList());

        this.matchers = matchers.stream()
                                .peek(item -> item.setIndex(classList.indexOf(item.getClass())))
                                .sorted()
                                .collect(Collectors.toList());

    }

    @Override
    public Iterator<ProfileMatcher> matcherIterator() {
        return matchers.iterator();
    }

}
