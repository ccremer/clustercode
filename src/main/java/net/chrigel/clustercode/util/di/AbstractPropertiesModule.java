package net.chrigel.clustercode.util.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import net.chrigel.clustercode.util.ConfigurationHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Provides a guice module which loads and applies settings from an external properties file.
 */
public abstract class AbstractPropertiesModule extends AbstractModule {

    /**
     * Binds the environmental system variables from the given map of keys. The value of the key is the default value
     * if the environment variable is not defined.
     *
     * @param map the map of keys with their default values.
     */
    protected final void bindEnvironmentVariablesWithDefaults(Map<String, String> map) {
        bindProperties(ConfigurationHelper.getEnvironmentVariablesWithDefaults(map));
    }

    /**
     * Binds the environmental system variables from the given map of keys. The value of the key is the default value
     * if the environment variable is not defined. Only the keys found in the expected list will be bound, even if
     * the map provided more.
     *
     * @param map      the map of keys with their default values.
     * @param expected the list of expected keys.
     */
    protected final void bindEnvironmentVariablesWithDefaults(Map<String, String> map, List<String> expected) {
        bindProperties(ConfigurationHelper.getEnvironmentVariablesWithDefaults(map.entrySet()
                .stream()
                .filter(entry -> expected.contains(entry.getKey()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue))));
    }

    /**
     * Binds the environmental system variables from the given map of keys. The value of the key is the default value
     * if the environment variable is not defined. The entries will be casted to string with {@link Object#toString()}
     *
     * @param map the map of keys with their default values.
     */
    protected final void bindEnvironmentVariablesWithDefaultsByObject(Map<Object, Object> map) {
        bindProperties(ConfigurationHelper.getEnvironmentVariablesWithDefaults(map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()))));
    }

    /**
     * Binds the environmental system variables from the given map of keys. The value of the key is the default value
     * if the environment variable is not defined. The entries will be casted to string with
     * {@link Object#toString()}. Only the keys found in the expected list will be bound, even if the map provided more.
     *
     * @param map      the map of keys with their default values.
     * @param expected the list of expected keys.
     */
    protected final void bindEnvironmentVariablesWithDefaultsByObject(Map<Object, Object> map, List<Object> expected) {
        bindProperties(ConfigurationHelper.getEnvironmentVariablesWithDefaults(map.entrySet()
                .stream()
                .filter(entry -> expected.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()))));
    }

    protected final void bindProperties(Map<String, String> map) {
        Names.bindProperties(binder(), map);
    }

    /**
     * Binds the environmental system variables from the given list of keys. If the variable is undefined for a key
     * in the list, the variable will not be bound.
     *
     * @param keys the list of expected keys.
     */
    protected final void bindEnvironmentVariablesOrSkip(List<String> keys) {
        Names.bindProperties(binder(), ConfigurationHelper.getEnvironmentalVariablesFromKeys(keys));
    }

    /**
     * Loads the properties from the given file. The properties will be loaded using {@link
     * ConfigurationHelper#loadPropertiesFromFile(String)}. In case of an IO error, the error will be added using {@link
     * #addError(Throwable)}.
     *
     * @param fileName the file name.
     * @return An optional with the properties object, empty on errors.
     */
    protected final Optional<Properties> loadPropertiesFromFile(String fileName) {
        try {
            return Optional.of(ConfigurationHelper.loadPropertiesFromFile(fileName));
        } catch (IOException e) {
            addError(e);
            return Optional.empty();
        }
    }

    /**
     * Gets the property value of the given properties and key. If the value could not be found (or is in fact null), an
     * error will be added using {@link #addError(String, Object...)} and en empty string returned.
     *
     * @param properties the properties to search in.
     * @param key        the property key.
     * @return the string value of the property
     */
    protected final String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value != null) return value;
        addError("Property " + key + " is not set.");
        return "";
    }

    /**
     * Gets the environment variable of the given key. Reverts to the given properties if key is not specified. If the
     * value could not be found (or is in fact null), an
     * error will be added using {@link #addError(String, Object...)} and en empty string returned.
     *
     * @param defaults the properties object with default values.
     * @param key      the property key.
     * @return the string value of the environment variable. Empty string if error.
     */
    protected final String getEnvironmentVariableOrProperty(Properties defaults, String key) {
        String var = System.getenv(key);
        if (var == null) var = defaults.getProperty(key);
        if (null != var) return var;
        addError("Property " + key + " is not set.");
        return "";
    }

    /**
     * Gets the environment variable of the given key. Reverts to the given properties if key is not specified. If the
     * value could not be found (or is in fact null), an
     * error will be added using {@link #addError(String, Object...)} and en empty string returned.
     *
     * @param defaults the properties object with default values.
     * @param key      the property key.
     * @return the string value of the environment variable. Empty string if error.
     */
    protected final String getEnvironmentVariableOrPropertyIgnoreError(Properties defaults, String key, String defaultValue) {
        String var = System.getenv(key);
        if (var == null) var = defaults.getProperty(key);
        if (var == null) return defaultValue;
        return var;
    }
}
