package youthm2.bootstrap.model;

import youthm2.bootstrap.model.program.Program;

/**
 * ProgramModel
 *
 * @author qiang.zhang
 */
final class ProgramModel {

  boolean start(Program program) {
    if (program.enabled) {
      switch (program.status) {
        case DEFAULT:
          program.exec();
          return false;
        case STARTING:
          // fixme 通常在执行之后，自动检测是否成功运行，这里为了测试暂时写成这样
          program.status = Program.Status.RUNNING;
          return false;
        case ERROR:
          throw new RuntimeException("启动出错：" + program.path);
        case RUNNING:
        default:
          break;
      }
    }
    return true;
  }

  boolean stop(Program program) {
    return true;
  }

  void sendMessage(Program program, String content) {

  }
}
