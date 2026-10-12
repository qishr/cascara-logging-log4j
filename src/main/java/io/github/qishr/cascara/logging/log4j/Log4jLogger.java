package io.github.qishr.cascara.logging.log4j;

import io.github.qishr.cascara.common.diagnostic.Diagnostic;
import io.github.qishr.cascara.common.diagnostic.log.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import java.time.ZoneId;

public class Log4jLogger implements Logger {

    private final org.apache.logging.log4j.core.Logger coreLogger;

    public Log4jLogger() {
        // Obtain the core Logger instance
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        this.coreLogger = context.getLogger("Cascara");
    }

    @Override
    public void log(Diagnostic diagnostic) {
        if (diagnostic == null) return;

        // Convert Diagnostic's LocalDateTime / Instant to epoch millis
        long epochMillis = diagnostic.getTimestamp()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

        // 1. Build an explicit Log4j LogEvent retaining original thread and timestamp
        Log4jLogEvent event = Log4jLogEvent.newBuilder()
            .setLoggerName(diagnostic.getSource() != null ? diagnostic.getSource() : coreLogger.getName())
            .setLevel(mapLevel(diagnostic.getLevel()))
            .setMessage(new SimpleMessage(diagnostic.getFormattedMessage()))
            .setThreadName(diagnostic.getThread()) // Preserves child JVM origin thread!
            .setTimeMillis(epochMillis)
            .setThrown(diagnostic.getCause())
            .build();

        // 2. Dispatch directly to Log4j's LoggerConfig pipeline
        Configuration config = coreLogger.getContext().getConfiguration();
        config.getLoggerConfig(coreLogger.getName()).log(event);
    }

    private Level mapLevel(Diagnostic.Level level) {
        if (level == null) return Level.INFO;
        return switch (level) {
            case TRACE -> Level.TRACE;
            case DEBUG -> Level.DEBUG;
            case WARN  -> Level.WARN;
            case ERROR -> Level.ERROR;
            default    -> Level.INFO;
        };
    }

    @Override
    public void flush() {}
}