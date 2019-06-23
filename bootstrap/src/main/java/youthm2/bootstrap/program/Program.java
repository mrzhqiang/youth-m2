package youthm2.bootstrap.program;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import youthm2.bootstrap.config.ProgramConfig;

/**
 * Program
 *
 * @author qiang.zhang
 */
public final class Program {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

  public State state;
  public String path;
  public Process process;
  public String handler;
  public String bootstrapHanlder;

  public ProgramConfig config;

  public void init(String path, ProgramConfig config) {
    this.path = path;
    this.config = config;
  }

  public void run() {
    String command = String.format(Locale.getDefault(),
        "%s%s %s %d %d", path, config.filename, handler, config.x, config.y);
    try {
      process = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      LOGGER.error("启动程序 [{}] 出错！", config.filename, e);
    }
  }

  public enum State {
    DEFAULT("默认"),
    STARTING("正在启动"),
    RUNNING("运行中"),
    STOPPING("正在停止"),
    STOPED("已停止"),
    FAILED("失败"),
    ERROR("错误"),
    ;

    private final String message;

    State(String message) {
      this.message = message;
    }

    @Override public String toString() {
      return message;
    }
  }
}
