package clustercode.impl.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileMatcher;
import clustercode.impl.scan.matcher.ProfileMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProfileScanServiceImplTest {

    private ProfileScanServiceImpl subject;

    @Mock
    private Media candidate;
    @Mock
    private ProfileMatcher matcher1;
    @Mock
    private ProfileMatcher matcher2;

    @Spy
    private Profile profile;

    private Map<ProfileMatchers, ProfileMatcher> matchers = new HashMap<>();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        matchers.put(ProfileMatchers.DIRECTORY_STRUCTURE, matcher1);
        matchers.put(ProfileMatchers.DEFAULT, matcher2);
        subject = new ProfileScanServiceImpl(matchers);
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
