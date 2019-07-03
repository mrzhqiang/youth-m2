package bootstrap.model;

import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序模块。
 *
 * @author qiang.zhang
 */
final class ProgramModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

  private final Map<String, Program> allProgram = Maps.newHashMap();

  void start(String command) {
    Program program = allProgram.get(command);
    if (program == null) {
      program = new Program();
      allProgram.put(command, program);
    }
    program.status = Status.STARTING;
    ProcessBuilder pb = new ProcessBuilder(command);
    try {
      File file = new File("nohub-youthm2.out");
      if (!file.exists() && file.createNewFile()) {
        LOGGER.info("create not exists file: " + file.toString());
      }
      pb.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
      program.process = pb.start();
      program.status = Status.RUNNING;
    } catch (IOException e) {
      LOGGER.error("启动 ["+command+"] 出错", e);
      program.status = Status.ERROR_OF_START;
    }
  }

  boolean check(String command) {
    Program program = allProgram.get(command);
    return program != null && program.process.isAlive();
  }

  private static final class Program {
    String command;
    Status status = Status.STOPPED;
    Process process;
  }

  enum Status {
    STOPPED,
    STARTING,
    ERROR_OF_START,
    RUNNING,
    DIRTY_SHUTDOWN,
    STOPPING,
    ERROR_OF_STOP
  }
}
