package clustercode.impl.scan.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileParser;
import clustercode.impl.scan.ProfileScanConfig;
import clustercode.test.util.FileBasedUnitTest;
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
    private ProfileScanConfig config;
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
        when(config.profile_file_name_extension()).thenReturn(".ffmpeg");
        when(config.profile_base_dir()).thenReturn(profileFolder);
        when(config.default_profile_file_name()).thenReturn("default");
        subject = new DefaultProfileMatcher(config, parser);
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
