package youthm2.common;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * CommandUtil
 *
 * @author mrzhqiang
 */
public final class CommandUtil {
    private static final Logger logger = LoggerFactory.getLogger("common");

    private CommandUtil() {
        throw new AssertionError("No instance.");
    }

    @Nullable
    public static Process execute(String command) {
        Preconditions.checkNotNull(command, "command == null");
        Runtime runtime = Runtime.getRuntime();
        try {
            return runtime.exec(command);
        } catch (IOException e) {
            logger.error("Start [" + command + "] failed.", e);
        }
        return null;
    }
}
