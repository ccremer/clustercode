package net.chrigel.clustercode.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;

public class ConfigurationHelperTest {

    @Test
    public void getEnvironmentalVariablesFromKeys_ShouldReturnGivenEntry() throws Exception {
        boolean testEnabled = isLinux() || isWindows();
        if (testEnabled) {
            List<String> keys = Arrays.asList(getSampleEnvironmentKey(), "HOPEFULLY_INEXISTENT_VARIABLE");
            String variable = System.getenv(getSampleEnvironmentKey());
            Map<String, String> results = ConfigurationHelper.getEnvironmentalVariablesFromKeys(keys);
            assertThat(results, hasKey(getSampleEnvironmentKey()));
            assertThat(results, hasValue(variable));
            assertThat(results.size(), equalTo(1));
        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name", "generic").toLowerCase();
        return os.indexOf("win") >= 0;
    }

    private boolean isLinux() {
        String os = System.getProperty("os.name", "generic").toLowerCase();
        return os.indexOf("nux") >= 0;
    }

    private String getSampleEnvironmentKey() {
        if (isWindows()) {
            return "USERNAME";
        }
        if (isLinux()) {
            return "USER";
        }
        return null;
    }

    @Test
    public void bindEnvironmentWithDefault_ShouldCreateMapWithOneEntry_AndApplyDefault() throws Exception {

        String key1 = getSampleEnvironmentKey();
        String value1 = "DEFAULT_VALUE1";

        String key2 = "DEFAULT_KEY1";
        String value2 = "DEFAULT_VALUE2";

        Map<String, String> testMap = new HashMap<>();
        testMap.put(key1, value1);
        testMap.put(key2, value2);

        Map<String, String> results = ConfigurationHelper.getEnvironmentVariablesWithDefaults(testMap);


        assertThat(results, hasValue(System.getenv(getSampleEnvironmentKey())));
        assertThat(results, hasValue(value2));
    }
}