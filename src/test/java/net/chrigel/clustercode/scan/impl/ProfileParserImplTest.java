package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.test.FileBasedUnitTest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

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

        assertThat(results).containsExactly("option", "with", "space", "another", "line");
    }

    @Test
    public void parseFile_ShouldIgnoreFieldLines() throws Exception {
        Path testFile = getPath("profile.ffmpeg");
        String option1 = " %{FIELD=value}";
        String option2 = "another line";
        Files.write(testFile, Arrays.asList(option1, option2));

        List<String> results = subject.parseFile(testFile).get().getArguments();

        assertThat(results).containsExactly("another", "line");
    }

    @Test
    public void parseFile_ShouldParseFieldLines() throws Exception {
        Path testFile = getPath("profile.ffmpeg");
        String option1 = "%{FIELD=value}";
        String option2 = "%{key=other}";
        Files.write(testFile, Arrays.asList(option1, option2));

        Map<String, String> results = subject.parseFile(testFile).get().getFields();

        assertThat(results)
                .containsKeys("FIELD", "KEY")
                .containsValues("value", "other")
                .hasSize(2);
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

    @Test
    public void extractKey_ShouldReturnKey_InUppercase() throws Exception {
        String testLine = "%{key=value}";
        assertThat(subject.extractKey(testLine)).isEqualTo("KEY");
    }

    @Test
    public void isFieldLine_ShouldReturnTrue_IfLineIsFieldLine() throws Exception {
        String testLine = "%{KEY=value}";
        assertThat(subject.isFieldLine(testLine)).isTrue();
    }

    @Test
    public void isFieldLine_ShouldReturnFalse_IfLineDoesNotBeginWithPercent() throws Exception {
        String testLine = "{KEY=value}";
        assertThat(subject.isFieldLine(testLine)).isFalse();
    }

    @Test
    public void isFieldLine_ShouldReturnFalse_IfKeyIsInvalid() throws Exception {
        String testLine = "%{=value}";
        assertThat(subject.isFieldLine(testLine)).isFalse();
    }

    @Test
    public void isFieldLine_ShouldReturnTrue_IfKeyIsValid_AndNoValuePresent() throws Exception {
        String testLine = "%{key=}";
        assertThat(subject.isFieldLine(testLine)).isTrue();
    }

    @Test
    public void isFieldLine_ShouldReturnFalse_IfKeyIsValid_AndNoNoClosingBracket() throws Exception {
        String testLine = "%{key=";
        assertThat(subject.isFieldLine(testLine)).isFalse();
    }

    @Test
    public void extractValue_ShouldReturnValue() throws Exception {
        String testLine = "%{KEY=.value}";
        assertThat(subject.extractValue(testLine)).isEqualTo(".value");
    }

    @Test
    public void extractValue_ShouldReturnEmptyString_IfNoValuePresent() throws Exception {
        String testLine = "%{KEY=}";
        assertThat(subject.extractValue(testLine)).isEqualTo("");
    }

    @Test
    public void separateWhitespace_ShouldReturnTwoElements() throws Exception {
        String testLine = "one two";
        assertThat(subject.separateWhitespace(testLine))
            .contains("one", atIndex(0))
            .contains("two", atIndex(1))
            .hasSize(2);
    }

    @Test
    public void separateWhitespace_ShouldReturnOneElement_IfNoWhitespace() throws Exception {
        String testLine = "one";
        assertThat(subject.separateWhitespace(testLine))
            .contains("one", atIndex(0))
            .hasSize(1);
    }
    @Test
    public void separateWhitespace_ShouldReturnOneElement_IfWhitespaceIsQuoted() throws Exception {
        String testLine = "\"one two \"";
        assertThat(subject.separateWhitespace(testLine))
            .contains("\"one two \"", atIndex(0))
            .hasSize(1);
    }
}
