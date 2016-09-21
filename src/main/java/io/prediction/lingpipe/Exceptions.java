package io.prediction.lingpipe;

import org.apache.log4j.Logger;

public class Exceptions {
    private static final Logger log = Logger.getLogger(Exceptions.class);

    public static void log(Throwable cause) {
        log.error(cause.getMessage(), cause);
    }

    public static void log(String customMessage, Throwable cause) {
        log.error(customMessage+": "+cause.getMessage(), cause);
    }

    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new RuntimeException(ex);
    }
}
