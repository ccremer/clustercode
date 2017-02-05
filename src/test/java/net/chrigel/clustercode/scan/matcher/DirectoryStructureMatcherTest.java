package net.chrigel.clustercode.scan.matcher;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileParser;
import net.chrigel.clustercode.scan.ProfileScanSettings;
import net.chrigel.clustercode.scan.Media;
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

public class DirectoryStructureMatcherTest implements FileBasedUnitTest {

    private DirectoryStructureMatcher subject;
    @Mock
    private ProfileParser parser;
    @Mock
    private ProfileScanSettings settings;
    @Mock
    private Media candidate;
    @Spy
    private Profile profile;
    private Path profileFolder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        profileFolder = getPath("profiles");
        when(settings.getProfileFileName()).thenReturn("profile");
        when(settings.getProfileFileNameExtension()).thenReturn(".ffmpeg");
        when(settings.getProfilesBaseDir()).thenReturn(profileFolder);
        subject = new DirectoryStructureMatcher(parser, settings);
    }

    @Test
    public void apply_ShouldReturnProfile_ClosestToOriginalStructure() throws Exception {
        Path media = createFile(getPath("0", "movies", "subdir", "movie.mp4"));
        Path profileFile = createFile(profileFolder.resolve("0/movies/subdir/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));

        Optional<Profile> result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }

    @Test
    public void apply_ShouldReturnProfile_FromParentDirectory() throws Exception {
        Path media = createFile(getPath("0", "movies", "subdir", "movie.mp4"));
        Path profileFile = createFile(profileFolder.resolve("0/movies/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));

        Optional<Profile> result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }

    @Test
    public void apply_ShouldReturnProfile_FromGrandParentDirectory() throws Exception {
        Path media = createFile(getPath("0", "movies", "subdir", "movie.mp4"));
        Path profileFile = createFile(profileFolder.resolve("0/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));
        profile.setLocation(profileFile);

        Optional<Profile> result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }


    @Test
    public void apply_ShouldReturnEmptyProfile_IfNoFileMatches() throws Exception {
        Path media = createFile(getPath("0", "movies", "subdir", "movie.mp4"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(any())).thenReturn(Optional.empty());

        Optional<Profile> result = subject.apply(candidate);

        assertThat(result).isEmpty();
    }

    @Test
    public void apply_ShouldReturnParentProfile_IfSiblingFileCouldNotBeRead() throws Exception {
        Path media = createFile(getPath("0", "movies", "subdir", "movie.mp4"));
        createFile(profileFolder.resolve("0/movies/subdir/profile.ffmpeg"));
        createFile(profileFolder.resolve("0/movies/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(any())).thenReturn(Optional.empty(), Optional.of(profile));

        Optional<Profile> result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }

}