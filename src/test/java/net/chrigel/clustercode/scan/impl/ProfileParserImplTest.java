package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileParserImplTest implements FileBasedUnitTest {

    private ProfileParserImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new ProfileParserImpl();
        setupFileSystem();
    }

    @Test
    public void parseFile_ShouldIgnoreEmptyLine() throws Exception {
        Path testFile = getPath("profile.ffmpeg");
        String option1 = " option with space";
        String option2 = "another line";
        Files.write(testFile, Arrays.asList(option1, "", option2));

        List<String> results = subject.parseFile(testFile).get().getArguments();

        assertThat(results).containsExactly(option1.trim(), option2);
    }

    @Test
    public void parseFile_ShouldReturnEmptyProfile_OnError() throws Exception {
        Path testFile = getPath("profile.ffmpeg");

        Optional<Profile> result = subject.parseFile(testFile);

        assertThat(result).isEmpty();
    }

    @Test
    public void isCommentLine_ShouldReturnTrue_IfLineBeginsWithHashtag() throws Exception {
        String testLine = "# this is a comment";
        assertThat(subject.isCommentLine(testLine)).isTrue();
    }

    @Test
    public void isCommentLine_ShouldReturnTrue_IfLineIsEmpty() throws Exception {
        String testLine = "";
        assertThat(subject.isCommentLine(testLine)).isTrue();
    }

    @Test
    public void isCommentLine_ShouldReturnFalse_IfLineIsValid() throws Exception {
        String testLine = "this is not a comment";
        assertThat(subject.isCommentLine(testLine)).isFalse();
    }

}