package net.chrigel.clustercode.util.di;

import net.chrigel.clustercode.util.InvalidConfigurationException;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ModuleHelperTest {

    @Test
    public void sortImplementations_ShouldReturnEmptyList_IfSetIsEmpty() throws Exception {

        String key = "CLASS_A CLASS_B";
        List<TestInterface> result = ModuleHelper.sortImplementations(key, new HashSet<>(), Implementations::valueOf);

        assertThat(result).isEmpty();
    }

    @Test
    public void sortImplementations_ShouldReturnSameList_IfSetIsOrdered() throws Exception {
        String key = "CLASS_A CLASS_B";
        Set<TestInterface> set = new HashSet<>();
        Collections.addAll(set, new TestClassA(), new TestClassB());
        List<TestInterface> result = ModuleHelper.sortImplementations(key, set, Implementations::valueOf);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isInstanceOf(TestClassA.class);
        assertThat(result.get(1)).isInstanceOf(TestClassB.class);
    }

    @Test
    public void sortImplementations_ShouldReturnOrderedList_IfSetIsUnordered() throws Exception {
        String key = "CLASS_B CLASS_A CLASS_C";
        Set<TestInterface> set = new HashSet<>();
        Collections.addAll(set, new TestClassA(), new TestClassB(), new TestClassC());
        List<TestInterface> result = ModuleHelper.sortImplementations(key, set, Implementations::valueOf);

        assertThat(result).hasSize(3);
        assertThat(result.get(1)).isInstanceOf(TestClassA.class);
        assertThat(result.get(0)).isInstanceOf(TestClassB.class);
        assertThat(result.get(2)).isInstanceOf(TestClassC.class);
    }

    @Test
    public void sortImplementations_ShouldReturnSameList_IfSetContainsOneValue() throws Exception {
        String key = "CLASS_B";
        Set<TestInterface> set = new HashSet<>();
        Collections.addAll(set, new TestClassB());
        List<TestInterface> result = ModuleHelper.sortImplementations(key, set, Implementations::valueOf);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(TestClassB.class);
    }

    @Test
    public void checkStrategiesForIncompatibilities_ShouldThrowException_IfStrategiesAreIncompatible() throws
            Exception {
        String key = "CC_STRATEGY";
        String value1 = Implementations.CLASS_A.name();
        String value2 = Implementations.CLASS_B.name();
        String strategies = value1.concat(" ").concat(value2);

        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
                ModuleHelper.checkStrategiesForIncompatibilities(strategies, key, value1, value2));
    }

    @Test
    public void checkStrategiesForIncompatibilities_ShouldDoNothing_IfStrategiesAreCompatible() throws Exception {
        String key = "CC_STRATEGY";
        String value1 = Implementations.CLASS_A.name();
        String value2 = Implementations.CLASS_B.name();
        String strategies = value1.concat(" ").concat(Implementations.CLASS_C.name());

        ModuleHelper.checkStrategiesForIncompatibilities(strategies, key, value1, value2);
    }

    @Test
    public void checkStrategiesForOrder_ShouldThrowException_IfOrderIsInvalid() throws Exception {
        String key = "CC_STRATEGY";
        String value1 = Implementations.CLASS_A.name();
        String value2 = Implementations.CLASS_B.name();
        String strategies = value2.concat(" ").concat(value1);

        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
                ModuleHelper.checkStrategiesForOrder(strategies, key, value1, value2));
    }

    @Test
    public void checkStrategiesForOrder_ShouldDoNothing_IfOrderIsValid() throws Exception {
        String key = "CC_STRATEGY";
        String value1 = Implementations.CLASS_A.name();
        String value2 = Implementations.CLASS_B.name();
        String strategies = value1.concat(" ").concat(value2);

        ModuleHelper.checkStrategiesForOrder(strategies, key, value1, value2);
    }

    enum Implementations implements EnumeratedImplementation<TestInterface> {
        CLASS_A(TestClassA.class),
        CLASS_B(TestClassB.class),
        CLASS_C(TestClassC.class);

        private final Class<? extends TestInterface> impl;

        Implementations(Class<? extends TestInterface> clazz) {
            this.impl = clazz;
        }

        @Override
        public Class<? extends TestInterface> getImplementingClass() {
            return impl;
        }
    }

    interface TestInterface {

    }

    class TestClassA implements TestInterface {

    }

    class TestClassB implements TestInterface {

    }

    class TestClassC implements TestInterface {

    }
}