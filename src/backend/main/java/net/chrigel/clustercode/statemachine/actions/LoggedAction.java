package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import org.slf4j.ext.LoggerWrapper;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;
import java.util.function.BiConsumer;

public class LoggedAction extends Action {

    private XLogger log = XLoggerFactory.getXLogger(getClass());
    private Loggers level = Loggers.INFO;
    private String statement;

    public LoggedAction() {
    }

    public LoggedAction(String statement) {
        this.statement = statement;
    }

    public LoggedAction(String formatString, Object... args) {
        this(MessageFormatter.arrayFormat(formatString, args).getMessage());
    }

    public LoggedAction withStatement(String statement) {
        this.statement = statement;
        return this;
    }

    public LoggedAction withStatement(String formatString, Object... args) {
        this.statement = MessageFormatter.arrayFormat(formatString, args).getMessage();
        return this;
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
    public StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        level.logWith(log, statement);
        return StateEvent.FINISHED;
    }

    private enum Loggers {

        TRACE(LoggerWrapper::trace),
        DEBUG(LoggerWrapper::debug),
        INFO(LoggerWrapper::info),
        WARN(LoggerWrapper::warn),
        ERROR(LoggerWrapper::error);

        private final BiConsumer<XLogger, String> consumer;

        Loggers(BiConsumer<XLogger, String> consumer) {
            this.consumer = consumer;
        }

        void logWith(XLogger logger, String statement) {
            consumer.accept(logger, statement);
        }
    }

}
