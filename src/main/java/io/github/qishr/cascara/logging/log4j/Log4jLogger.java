// # License & Terms
//
// This file is part of **Cascara**.
//
// **Cascara** is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
//
// ---
//
// ## Special Runtime Exception
//
// As a special exception, the copyright holders of this library give you
// permission to link this library with independent modules to produce an
// executable, regardless of the license terms of these independent modules,
// and to copy and distribute the resulting executable under terms of your
// choice, provided that you also meet, for each linked independent module,
// the terms and conditions of the license of that module.
//
// An independent module is a module which is not derived from or based on
// this library. If you modify this library, you may extend this exception
// to your version of the library, but you are not obligated to do so. If
// you do not wish to do so, delete this exception statement from your
// version.

package io.github.qishr.cascara.logging.log4j;

import io.github.qishr.cascara.common.diagnostic.Diagnostic;
import io.github.qishr.cascara.common.diagnostic.format.FormatOptions;
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
    public Log4jLogger log(Diagnostic diagnostic) {
        if (diagnostic == null) return this;

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
        return this;
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
    public Log4jLogger flush() {
        return this;
    }

    @Override
    public Logger setFormatOptions(FormatOptions options) {
        return this;
    }
}