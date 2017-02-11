package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;
import java.util.function.BiConsumer;

public class LoggedAction extends AbstractAction {

    private XLogger log = XLoggerFactory.getXLogger(getClass());
    private Loggers level = Loggers.INFO;
    private String statement;

    public LoggedAction(String statement) {
        this.statement = statement;
    }

    public LoggedAction(String formatString, Object... args) {
        this(MessageFormatter.arrayFormat(formatString, args).getMessage());
    }

    public LoggedAction withName(Class name) {
        this.log = XLoggerFactory.getXLogger(name);
        return this;
    }

    public LoggedAction withLevel(XLogger.Level level) {
        Objects.requireNonNull(level);
        this.level = Loggers.valueOf(level.name());
        return this;
    }

    @Override
    public void execute(State from, State to, StateEvent event, StateContext context, StateController stateMachine) {
        doExecute(from, to, event, context);
    }

    @Override
    public StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        level.logWith(log, statement);
        return StateEvent.FINISHED;
    }

    private enum Loggers {

        TRACE((logger, s) -> logger.trace(s)),
        DEBUG((logger, s) -> logger.debug(s)),
        INFO((logger, s) -> logger.info(s)),
        WARN((logger, s) -> logger.warn(s)),
        ERROR((logger, s) -> logger.error(s));

        private final BiConsumer<XLogger, String> consumer;

        Loggers(BiConsumer<XLogger, String> consumer) {
            this.consumer = consumer;
        }

        void logWith(XLogger logger, String statement) {
            consumer.accept(logger, statement);
        }
    }

}
