package net.chrigel.clustercode.util;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.util.List;
import java.util.Map;
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
                        entry -> entry.getKey(),
                        entry -> entry.getValue()))));
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

}
