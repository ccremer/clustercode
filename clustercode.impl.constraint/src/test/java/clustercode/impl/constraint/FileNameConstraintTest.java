package clustercode.impl.constraint;

import clustercode.api.domain.Media;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FileNameConstraintTest implements FileBasedUnitTest {

    private FileNameConstraint subject;
    @Mock
    private Media candidate;
    @Mock
    private ConstraintConfig config;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
    }

    @Test
    public void accept_ShouldReturnFalse_IfRegexOnlyMatchesPartially() throws Exception {
        when(candidate.getSourcePath()).thenReturn(getPath("input", "movie.mp4"));
        when(config.filename_regex()).thenReturn(".mp4");

        subject = new FileNameConstraint(config);
        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnTrue_IfRegexMatches() throws Exception {
        when(candidate.getSourcePath()).thenReturn(getPath("input", "movie.mp4"));
        when(config.filename_regex()).thenReturn("^.*\\.mp4");

        subject = new FileNameConstraint(config);
        assertThat(subject.accept(candidate)).isTrue();
    }

}
