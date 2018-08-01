package clustercode.api.config;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigLoaderTest {

    private ConfigLoader subject;
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        subject = new ConfigLoader();
    }

    @Test
    public void getConfig_ShouldLoadDefaultsFromAnnotations() {
        TestConfig result = subject.getConfig(TestConfig.class);

        assertThat(result.variable()).isEqualTo("unassigned");
    }

    @Test
    public void getConfig_ShouldOverrideDefaultsFromFile() {

        TestConfig result = subject
                .loadDefaultsFromPropertiesFile("src/test/resources/ConfigLoaderTest.properties")
                .getConfig(TestConfig.class);

        assertThat(result.variable()).isEqualTo("fromFile");
    }

    @Test
    public void getConfig_ShouldLoadDefaults_IfFileNotFound() {
        TestConfig result = subject
                .loadDefaultsFromPropertiesFile("src/test/resources/ConfigLoaderTest.inexistent")
                .getConfig(TestConfig.class);

        assertThat(result.variable()).isEqualTo("unassigned");
    }

    @Test
    public void getConfig_ShouldLoadEnumFromFile_InOrder() {

        TestConfig result = subject
                .loadDefaultsFromPropertiesFile("src/test/resources/ConfigLoaderTest.properties")
                .getConfig(TestConfig.class);

        assertThat(result.enums()).containsSequence(TestEnum.VALUE_2, TestEnum.VALUE_1);
    }

    @Test
    public void getConfig_ShouldThrowException_IfEnumInexistent() {
        expected.expect(UnsupportedOperationException.class);
        TestConfig result = subject
                .loadDefaultsFromPropertiesFile("src/test/resources/ConfigLoaderTest.properties")
                .getConfig(TestConfig.class);

        result.enum_inexistent();
    }


    @Ignore("This test works only if the env var test.environment is defined")
    public void getConfig_ShouldOverrideFromFileWithEnv() {

        TestConfig result = subject
                .loadDefaultsFromPropertiesFile("src/test/resources/ConfigLoaderTest.properties")
                .getConfig(TestConfig.class);

        assertThat(result.environment()).isEqualTo("fromEnv");
    }
}
