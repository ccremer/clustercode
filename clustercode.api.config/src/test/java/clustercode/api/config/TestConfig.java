package clustercode.api.config;

import org.aeonbits.owner.Config;

import java.util.List;

public interface TestConfig extends Config {

    @Key("test.variable")
    @DefaultValue("unassigned")
    String variable();

    @Key("test.environment")
    String environment();

    @Key("test.enum")
    List<TestEnum> enums();

    @Key("test.enum2")
    List<TestEnum> enum_inexistent();
}
