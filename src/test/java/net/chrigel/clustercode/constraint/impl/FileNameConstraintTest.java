package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FileNameConstraintTest implements FileBasedUnitTest {

    private FileNameConstraint subject;
    @Mock
    private MediaCandidate candidate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
    }

    @Test
    public void accept_ShouldReturnTrue_IfConstraintIsDisabled() throws Exception {
        subject = new FileNameConstraint(":");
        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfRegexOnlyMatchesPartially() throws Exception {
        when(candidate.getSourcePath()).thenReturn(getPath("input", "movie.mp4"));

        subject = new FileNameConstraint(".mp4");
        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnTrue_IfRegexMatches() throws Exception {
        when(candidate.getSourcePath()).thenReturn(getPath("input", "movie.mp4"));

        subject = new FileNameConstraint("^.*\\.mp4");
        assertThat(subject.accept(candidate)).isTrue();
    }


}