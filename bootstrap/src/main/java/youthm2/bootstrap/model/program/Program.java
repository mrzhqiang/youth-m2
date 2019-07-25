package youthm2.bootstrap.model.program;

import java.io.BufferedReader;
import youthm2.bootstrap.model.config.ProgramConfig;
import youthm2.common.dialog.ThrowableDialog;

/**
 * Program
 *
 * @author qiang.zhang
 */
public enum Program {
  DATABASE,
  ACCOUNT,
  LOGGER,
  CORE,
  GAME,
  ROLE,
  LOGIN,
  RANK,
  ;

  public boolean enabled;
  public Status status;
  public String path;
  public int x;
  public int y;
  public Process process;

  public void from(ProgramConfig config) {
    enabled = config.enabled;
    status = Program.Status.DEFAULT;
    path = config.path;
    x = config.x;
    y = config.y;
  }

  public void exec() {
    try {
      status = Status.STARTING;
      // fixme 正式测试应该打开这里的注释
      /*String command = String.format("%s %d %d", path, x, y);
      process = Runtime.getRuntime().exec(command);
      Observable.just(process)
          .subscribeOn(Schedulers.io())
          .map(Process::getInputStream)
          .map(InputStreamReader::new)
          .map(BufferedReader::new)
          .doOnNext(this::handleResponse)
          .subscribe();*/
    } catch (Exception e) {
      ThrowableDialog.show(e);
      status = Status.ERROR;
    }
  }

  private void handleResponse(BufferedReader bufferedReader) {
    do {
      try {
        String line = bufferedReader.readLine();
        if ("ok".equalsIgnoreCase(line)) {
          status = Program.Status.RUNNING;
          return;
        }
      } catch (Exception ignore) {
        status = Program.Status.ERROR;
        return;
      }
    } while (true);
  }

  public enum Status {
    DEFAULT,
    STARTING,
    RUNNING,
    ERROR,
  }
}
