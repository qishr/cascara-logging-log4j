package io.github.qishr.cascara.logging.log4j;

import io.github.qishr.cascara.common.diagnostic.message.GenericMessage;
import io.github.qishr.cascara.common.diagnostic.report.GlobalReporter;
import io.github.qishr.cascara.common.diagnostic.report.Reporter;

// import java.util.logging.Logger;

public class Main {
    private static final Reporter REPORTER = GlobalReporter.forClass(Main.class);

    public static void main(String[] args) {

        // 1. Disabled GlobalReporter's built-in console logging.
        GlobalReporter.globalInstance().setSystemOutputEnabled(false);

        // 2. Add the Log4jLogger
        GlobalReporter.globalInstance().addLogger(new Log4jLogger());

        REPORTER.error(GenericMessage.ERROR, "Hello, World!");
    }

}
