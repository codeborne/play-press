package press;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PressLogger {
    private static final Logger logger = LoggerFactory.getLogger(PressLogger.class);
    public static void trace(String message, Object... args) {
        logger.trace(message, args);
    }
}
