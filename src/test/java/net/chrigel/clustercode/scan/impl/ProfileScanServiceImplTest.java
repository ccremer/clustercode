package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.Media;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProfileScanServiceImplTest {

    private ProfileScanServiceImpl subject;

    @Mock
    private Media candidate;
    @Mock
    private ProfileMatcherStrategy strategy;
    @Mock
    private ProfileMatcher matcher1;
    @Mock
    private ProfileMatcher matcher2;

    @Spy
    private Profile profile;

    private List<ProfileMatcher> matchers;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new ProfileScanServiceImpl(strategy);
        matchers = Arrays.asList(matcher1, matcher2);
        when(strategy.matcherIterator()).thenReturn(matchers.iterator());
    }

    @Test
    public void selectProfile_ShouldReturnProfile_OfFirstMatcher() throws Exception {
        when(matcher1.apply(candidate)).thenReturn(Optional.of(profile));

        Profile result = subject.selectProfile(candidate).get();

        assertThat(result).isEqualTo(profile);
        verify(matcher1).apply(candidate);
        verify(matcher2, never()).apply(any());
    }

    @Test
    public void selectProfile_ShouldReturnProfile_OfSecondMatcher() throws Exception {
        when(matcher1.apply(candidate)).thenReturn(Optional.empty());
        when(matcher2.apply(candidate)).thenReturn(Optional.of(profile));

        Profile result = subject.selectProfile(candidate).get();

        assertThat(result).isEqualTo(profile);
        verify(matcher1).apply(candidate);
        verify(matcher2).apply(candidate);
    }

    @Test
    public void selectProfile_ShouldReturnEmpty_IfNoMatcherValid() throws Exception {
        when(matcher1.apply(candidate)).thenReturn(Optional.empty());
        when(matcher2.apply(candidate)).thenReturn(Optional.empty());

        Optional<Profile> result = subject.selectProfile(candidate);

        assertThat(result).isEmpty();
        verify(matcher1).apply(candidate);
        verify(matcher2).apply(candidate);
    }

}