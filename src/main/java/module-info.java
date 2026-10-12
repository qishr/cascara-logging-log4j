module cascara.logging.log4j {
    requires transitive cascara.common;
    requires java.management;

    // Java Logging
    requires java.logging;

    // Log4j
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports io.github.qishr.cascara.logging.log4j;
}
