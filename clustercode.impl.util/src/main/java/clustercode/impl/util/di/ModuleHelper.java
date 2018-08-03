package clustercode.impl.util.di;

import clustercode.impl.util.InvalidConfigurationException;

import java.util.List;

public class ModuleHelper {

    private ModuleHelper() {
    }

    public static <E extends Enum> StrategiesCheckerIntermediate<E> verifyIn(List<E> strategies) {
        return new StrategiesCheckerIntermediate<>(strategies);
    }

    public static class StrategiesCheckerIntermediate<E extends Enum> {

        private final List<E> strategies;

        private StrategiesCheckerIntermediate(List<E> strategies) {
            this.strategies = strategies;
        }

        public StrategiesChecker<E> that(E value1) {
            return new StrategiesChecker<>(value1, strategies);
        }
    }

    public static class StrategiesChecker<E extends Enum> {

        private final E value1;
        private final List<E> strategies;

        private StrategiesChecker(E value1, List<E> strategies) {
            this.value1 = value1;
            this.strategies = strategies;
        }

        /**
         * Checks if value1 is before value2 in the previously given {@code strategies} list. If it is not, an {@link
         * InvalidConfigurationException} is being thrown. Only checks if both values are present. It is assumed that
         * the
         * provided values are distinguishable (and not e.g. substring of each other).
         *
         * @param value2 the second value1.
         */
        public void isBefore(E value2) {
            if (strategies.contains(value1) && strategies.contains(value2)) {
                int index1 = strategies.indexOf(value1);
                int index2 = strategies.indexOf(value2);
                if (index1 >= index2) {
                    throw new InvalidConfigurationException(
                            "{} cannot be specified before {}. You configured: {}",
                            value2, value1, strategies);
                }
            }
        }

        /**
         * Checks if value1 AND value2 are in the previously given {@code strategies} list. If they are, an {@link
         * InvalidConfigurationException} is being thrown.
         *
         * @param value2 the second value.
         */
        public void isNotGivenTogetherWith(E value2) {
            if (strategies.contains(value1) && strategies.contains(value2)) {
                throw new InvalidConfigurationException(
                        "Config cannot contain {} and {} at the same time as they are incompatible. You configured: {}",
                        value1, value2, strategies);
            }
        }
    }
}
