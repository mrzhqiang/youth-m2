package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.model.config.ProgramConfig;
import youthm2.common.Environments;
import youthm2.common.Platform;

/**
 * 程序模块。
 *
 * @author qiang.zhang
 */
final class ProgramModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

  private final Map<String, Program> allProgram = Maps.newHashMap();

  void start(String home, ProgramConfig config) {
    Preconditions.checkNotNull(home, "home == null");
    Preconditions.checkNotNull(config, "config == null");
    if (!config.isEnabled()) {
      return;
    }
    File file = new File(config.getPath());
    if (file.isDirectory() || !file.exists()) {
      return;
    }
    String path = file.getPath();
    Program program = allProgram.get(path);
    if (program == null) {
      program = new Program();
      allProgram.put(path, program);
    }
    if (program.status == Status.INITIALIZED) {
      List<String> command = prepareCommand(path, config);
      ProcessBuilder builder = new ProcessBuilder(command);
      File directory = new File(home);
      if (Environments.isDebug()) {
        directory = new File(Environments.workDirectory(), home);
      }
      builder.directory(directory);
      try {
        program.process = builder.start();
        program.status = Status.STARTING;
      } catch (IOException e) {
        LOGGER.error("无法启动程序：{}", path);
        throw new RuntimeException(String.format(Locale.getDefault(),
            "启动程序 [%s] 出错！", path));
      }
    }
  }

  private List<String> prepareCommand(String path, ProgramConfig config) {
    Preconditions.checkNotNull(path, "path == null");
    Preconditions.checkNotNull(config, "config == null");

    List<String> command = Lists.newArrayList();
    Platform current = Platform.current();
    path = path.replace('/', current.fileSeparator);
    command.add(path);
    command.add(String.valueOf(config.getPort()));
    command.add(String.valueOf(config.getX()));
    command.add(String.valueOf(config.getY()));
    return command;
  }

  boolean check(String home, ProgramConfig config) {
    Preconditions.checkNotNull(home, "home == null");
    Preconditions.checkNotNull(config, "config == null");

    File file = new File(config.getPath());
    if (file.isDirectory() || !file.exists()) {
      return true;
    }
    String path = file.getPath();
    Program program = allProgram.get(path);
    return config.isEnabled() && program != null && program.status == Status.STARTED;
  }

  private static final class Program {
    Status status = Status.INITIALIZED;
    Process process;
  }

  enum Status {
    INITIALIZED,
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
  }
}
