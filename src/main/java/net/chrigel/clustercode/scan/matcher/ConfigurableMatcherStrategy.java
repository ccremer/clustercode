package net.chrigel.clustercode.scan.matcher;

import net.chrigel.clustercode.scan.ProfileMatcher;
import net.chrigel.clustercode.scan.ProfileMatcherStrategy;
import net.chrigel.clustercode.scan.impl.ScanModule;
import net.chrigel.clustercode.util.di.ModuleHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConfigurableMatcherStrategy implements ProfileMatcherStrategy {

    private final List<ProfileMatcher> matchers;

    @Inject
    ConfigurableMatcherStrategy(@Named(ScanModule.PROFILE_STRATEGY_KEY) String strategies,
                                Set<ProfileMatcher> matchers) {
        this.matchers = ModuleHelper.sortImplementations(strategies, matchers, ProfileMatchers::valueOf);
    }

    @Override
    public Iterator<ProfileMatcher> matcherIterator() {
        return matchers.iterator();
    }

}
