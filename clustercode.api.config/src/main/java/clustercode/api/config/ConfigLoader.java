package clustercode.api.config;

import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ConfigLoader {

    private List<Map<?, ?>> props = new ArrayList<>(Collections.singleton(System.getenv()));

    public <T extends Config> T getConfig(Class<T> type) {
        T config = ConfigFactory.create(type, props.toArray(new Map<?, ?>[0]));
        log.debug("{}: {}", type.getSimpleName(), config);
        return config;
    }

    public ConfigLoader loadDefaultsFromPropertiesFile(String filename) {
        try {
            props.add(loadFromFile(filename));
            return this;
        } catch (IOException ex) {
            log.warn("Could not find or read '{}'. If properties are missing, it will revert to hardcoded defaults" +
                    ".\n{}", filename, ex);
            return this;
        }
    }

    private Properties loadFromFile(String filename) throws IOException {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            props.load(reader);
            return props;
        }
    }
}
