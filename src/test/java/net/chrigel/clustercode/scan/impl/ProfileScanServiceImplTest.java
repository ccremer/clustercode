package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.MediaCandidate;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ProfileScanServiceImplTest implements FileBasedUnitTest {

    private ProfileScanServiceImpl subject;

    @Mock
    private MediaCandidate candidate;
    @Mock
    private ProfileMatcherStrategy strategy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupFileSystem();
        subject = new ProfileScanServiceImpl(strategy);
    }

    @Ignore
    @Test
    public void selectProfile_ShouldReturnProfile_IfSpecificProfileExists() throws Exception {
        Path media = getPath("/input", "movie.mp4");
        Path profileFile = getPath("/input", "movie.ffmpeg");
        Files.createFile(profileFile);

        when(candidate.getSourcePath()).thenReturn(media);

        Profile result = subject.selectProfile(candidate).get();

        assertThat(result.getLocation()).isEqualByComparingTo(profileFile);
    }


}