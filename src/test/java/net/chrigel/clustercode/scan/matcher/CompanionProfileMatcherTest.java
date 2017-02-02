package net.chrigel.clustercode.scan.matcher;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileParser;
import net.chrigel.clustercode.scan.ProfileScanSettings;
import net.chrigel.clustercode.task.Media;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CompanionProfileMatcherTest implements FileBasedUnitTest {

    @Mock
    private ProfileScanSettings settings;
    @Mock
    private ProfileParser profileParser;
    @Mock
    private Media candidate;
    @Spy
    private Profile profile;

    private CompanionProfileMatcher subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        subject = new CompanionProfileMatcher(settings, profileParser);
        when(settings.getProfileFileNameExtension()).thenReturn(".ffmpeg");
        when(profileParser.parseFile(any())).thenReturn(Optional.of(profile));
    }

    @Test
    public void apply_ShouldReturnProfile_IfFileFoundAndReadable() throws Exception {
        Path media = createParentDirOf(getPath("input", "movie.mp4"));
        createFile(getPath("input", "movie.mp4.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);

        Profile result = subject.apply(candidate).get();

        assertThat(result).isEqualTo(profile);
    }

    @Test
    public void apply_ShouldReturnEmpty_IfFileNotFound() throws Exception {
        Path media = createParentDirOf(getPath("input", "movie.mp4"));

        when(candidate.getSourcePath()).thenReturn(media);

        assertThat(subject.apply(candidate)).isEmpty();
    }

    @Test
    public void apply_ShouldReturnEmpty_IfFileNotReadable() throws Exception {
        Path media = createParentDirOf(getPath("input", "movie.mkv"));
        Path file = createFile(getPath("input", "movie.mkv.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);

        when(profileParser.parseFile(file)).thenReturn(Optional.empty());

        assertThat(subject.apply(candidate)).isEmpty();
    }

}