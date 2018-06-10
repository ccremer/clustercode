package clustercode.impl.util;

import lombok.extern.slf4j.XSlf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XSlf4j
public class ConfigurationHelper {

    private ConfigurationHelper() {

    }

    /**
     * Filters the map of environment variables with the given list of keys. Only the variables with their key being
     * in the list of keys are being returned. May be empty.
     *
     * @param keys the list of expected keys.
     * @return a map of filtered environmental variables.
     */
    public static Map<String, String> getEnvironmentalVariablesFromKeys(List<String> keys) {
        return System.getenv()
            .entrySet()
            .stream()
            .filter(entry -> keys.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Creates a map with the system variables from the given map of keys. The value of the map is the default value
     * if the environment variable is not defined.
     *
     * @param map the map of keys with their default values.
     * @return a map with the default values provided by the input, but with overridden values from environmental
     * variables if their keys were found.
     */
    public static Map<String, String> getEnvironmentVariablesWithDefaults(Map<String, String> map) {
        return Stream.concat(map.entrySet().stream(), getEnvironmentalVariablesFromKeys(
            new ArrayList<>(map.keySet())).entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (value1, value2) -> {
                    if (map.containsValue(value1)) {
                        return value2;
                    } else {
                        return value1;
                    }
                }
            ));
    }

    /**
     * Attempts to load a properties file from the given file name. First it will be read in the classpath, if it
     * could not load the file, this method will look in the file system.
     *
     * @param fileName the file name.
     * @return the Properties file.
     * @throws IOException if the file could not be loaded.
     */
    public static Properties loadPropertiesFromFile(String fileName) throws IOException {
        Properties properties = new Properties();
        try {
            InputStream stream = ClassLoader.getSystemResourceAsStream(fileName);
            if (stream == null) {
                log.debug("Reading config file {} from file system.", fileName);
                Reader reader = Files.newBufferedReader(FilesystemProvider.getInstance().getPath(fileName));
                properties.load(reader);
                reader.close();
            } else {
                log.debug("Reading config file {} from classpath.", fileName);
                properties.load(stream);
                stream.close();
            }
        } finally {
            return properties;
        }
    }

}
