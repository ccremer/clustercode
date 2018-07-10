package clustercode.impl.scan.matcher;

import clustercode.api.scan.ProfileMatcher;
import clustercode.impl.scan.ProfileScanConfig;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

public class ConfigurableMatcherStrategyTest {

    @Mock
    private ProfileScanConfig config;

    private DirectoryStructureMatcher directoryStructureMatcher;

    private DefaultProfileMatcher defaultProfileMatcher;

    private CompanionProfileMatcher companionProfileMatcher;

    private ConfigurableMatcherStrategy subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        directoryStructureMatcher = new DirectoryStructureMatcher(config, null);
        defaultProfileMatcher = new DefaultProfileMatcher(config, null);
        companionProfileMatcher = new CompanionProfileMatcher(config, null);
    }

    @Test
    public void matcherIterator_ShouldOrderImplementations_WithTwoMatchers() {
        when(config.profile_matchers())
                .thenReturn(Arrays.asList(
                        ProfileMatchers.DIRECTORY_STRUCTURE,
                        ProfileMatchers.COMPANION));
        Set<ProfileMatcher> set = new HashSet<>();
        set.add(companionProfileMatcher);
        set.add(directoryStructureMatcher);
        subject = new ConfigurableMatcherStrategy(config, set);
        Assertions.assertThat(subject.matcherIterator())
                  .containsExactly(
                          directoryStructureMatcher,
                          companionProfileMatcher);
    }

    @Test
    public void matcherIterator_ShouldOrderImplementations_WithThreeMatchers() {
        when(config.profile_matchers())
                .thenReturn(Arrays.asList(
                        ProfileMatchers.DIRECTORY_STRUCTURE,
                        ProfileMatchers.COMPANION,
                        ProfileMatchers.DEFAULT));
        Set<ProfileMatcher> set = new HashSet<>();
        set.add(companionProfileMatcher);
        set.add(directoryStructureMatcher);
        set.add(defaultProfileMatcher);
        subject = new ConfigurableMatcherStrategy(config, set);
        Assertions.assertThat(subject.matcherIterator())
                  .containsExactly(
                          directoryStructureMatcher,
                          companionProfileMatcher,
                          defaultProfileMatcher);
    }
}
