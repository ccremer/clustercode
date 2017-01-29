package net.chrigel.clustercode.scan.impl.matcher;

import net.chrigel.clustercode.scan.impl.ProfileMatcher;
import net.chrigel.clustercode.scan.impl.ProfileMatcherStrategy;
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
    ConfigurableMatcherStrategy(@Named(ScanModule.PROFILE_STRATEGY_KEY) String strategiesString,
                                Set<ProfileMatcher> matchers) {
        this.matchers = ModuleHelper.sortImplementations(strategiesString, matchers, this::getImplementationClass);
    }

    private Class<? extends ProfileMatcher> getImplementationClass(String strategy) {
        return Matchers.valueOf(strategy).getImplementingClass();
    }

    @Override
    public Iterator<ProfileMatcher> matcherIterator() {
        return matchers.iterator();
    }

}
