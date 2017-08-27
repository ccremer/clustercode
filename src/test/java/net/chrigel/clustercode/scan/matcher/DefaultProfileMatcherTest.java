package net.chrigel.clustercode.scan.matcher;

import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileParser;
import net.chrigel.clustercode.scan.ProfileScanSettings;
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

public class DefaultProfileMatcherTest implements FileBasedUnitTest {


    private DefaultProfileMatcher subject;
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
        when(settings.getProfileFileNameExtension()).thenReturn(".ffmpeg");
        when(settings.getProfilesBaseDir()).thenReturn(profileFolder);
        when(settings.getDefaultProfileFileName()).thenReturn("default");
        subject = new DefaultProfileMatcher(parser, settings);
    }

    @Test
    public void apply_ShouldReturnDefaultProfile_IfFileFoundAndParsable() throws Exception {
        Path profileFile = createFile(profileFolder.resolve("default.ffmpeg"));

        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));
        assertThat(subject.apply(candidate)).hasValue(profile);

    }

    @Test
    public void apply_ShouldReturnEmptyProfile_IfFileNotFound() throws Exception {
        when(parser.parseFile(any())).thenReturn(Optional.empty());
        assertThat(subject.apply(candidate)).isEmpty();

    }

}