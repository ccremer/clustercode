package net.chrigel.clustercode.util.di;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ModuleHelperTest {

    @Test
    public void sortImplementations_ShouldReturnEmptyList_IfSetIsEmpty() throws Exception {

        String key = "CLASS_A CLASS_B";
        List<TestInterface> result = ModuleHelper.sortImplementations(key, new HashSet<>(),
                this::getImplementationClass);

        assertThat(result).isEmpty();
    }

    @Test
    public void sortImplementations_ShouldReturnSameList_IfSetIsOrdered() throws Exception {
        String key = "CLASS_A CLASS_B";
        Set<TestInterface> set = new HashSet<>();
        Collections.addAll(set, new TestClassA(), new TestClassB());
        List<TestInterface> result = ModuleHelper.sortImplementations(key, set, this::getImplementationClass);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isInstanceOf(TestClassA.class);
        assertThat(result.get(1)).isInstanceOf(TestClassB.class);
    }

    @Test
    public void sortImplementations_ShouldReturnOrderedList_IfSetIsUnordered() throws Exception {
        String key = "CLASS_B CLASS_A CLASS_C";
        Set<TestInterface> set = new HashSet<>();
        Collections.addAll(set, new TestClassA(), new TestClassB(), new TestClassC());
        List<TestInterface> result = ModuleHelper.sortImplementations(key, set, this::getImplementationClass);

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
        List<TestInterface> result = ModuleHelper.sortImplementations(key, set, this::getImplementationClass);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(TestClassB.class);
    }

    private Class<? extends TestInterface> getImplementationClass(String strategy) {
        switch (strategy) {
            case "CLASS_A":
                return TestClassA.class;
            case "CLASS_B":
                return TestClassB.class;
            case "CLASS_C":
                return TestClassC.class;
            default:
                throw new IllegalArgumentException();
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