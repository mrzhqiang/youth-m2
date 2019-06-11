package youthm2.common;

import com.google.common.base.Preconditions;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static void execute(String command) {
    Preconditions.checkNotNull(command, "command == null");
    Runtime runtime = Runtime.getRuntime();
    try {
      runtime.exec(command);
    } catch (IOException e) {
      logger.error("Start [" + command + "] failed.", e);
    }
  }
}
