package clustercode.impl.scan;

import clustercode.api.domain.Profile;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileParserImplTest implements FileBasedUnitTest {

    private ProfileParserImpl subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new ProfileParserImpl();
        setupFileSystem();
    }

    @Test
    public void parseFile_ShouldIgnoreEmptyLine() throws Exception {
        Path testFile = getPath("profile.ffmpeg");
        String option1 = " line_without_space";
        String option2 = "line with space";
        Files.write(testFile, Arrays.asList(option1, "", option2));

        List<String> results = subject.parseFile(testFile).get().getArguments();

        assertThat(results).containsExactly("line_without_space", "line with space");
    }

    @Test
    public void parseFile_ShouldIgnoreFieldLines() throws Exception {
        Path testFile = getPath("profile.ffmpeg");
        String option1 = " %{FIELD=value}";
        String option2 = "another line";
        Files.write(testFile, Arrays.asList(option1, option2));

        List<String> results = subject.parseFile(testFile).get().getArguments();

        assertThat(results).containsExactly("another line");
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
}
