package youthm2.bootstrap.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.model.config.ProgramConfig;

/**
 * 程序模块。
 *
 * @author qiang.zhang
 */
final class ProgramModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

  private final ProgramConfig config;

  private Status status = Status.DEFAULT;
  private Process process;

  ProgramModel(ProgramConfig config) {
    this.config = config;
  }

  void start() {
    if (config.isEnabled()) {
      if (status == Status.DEFAULT) {
        status = Status.STARTING;
        String path = config.getPath();
        String port = config.getPort();
        String x = config.getX();
        String y = config.getY();
        String command = String.format(Locale.getDefault(), "%s %s %s %s", path, port, x, y);
        try {
          process = Runtime.getRuntime().exec(command);
          Observable.just(process.getInputStream())
              .subscribeOn(Schedulers.io())
              .map(inputStream -> new InputStreamReader(inputStream, StandardCharsets.UTF_8))
              .map(BufferedReader::new)
              .doOnNext(this::handleResponse)
              .subscribe();
        } catch (Exception e) {
          status = Status.DEFAULT;
          LOGGER.error("无法启动程序：{} in {} port", path, port);
          ExceptionDialog dialog = new ExceptionDialog(e);
          dialog.setHeaderText("启动程序 [" + path + "] 出错！");
          dialog.show();
        }
      }
    }
  }

  boolean check() {
    if (!config.isEnabled()) {
      return true;
    }
    return status == Status.STARTED && process != null && process.isAlive();
  }

  void stop() {
    if (status == Status.STARTED) {
      status = Status.STOPPING;
      if (process != null && process.isAlive()) {
        process.destroy();
      }
    }
    status = Status.DEFAULT;
  }

  private void handleResponse(BufferedReader bufferedReader) {
    do {
      try {
        String line = bufferedReader.readLine();
        if ("ok".equalsIgnoreCase(line)) {
          status = Status.STARTED;
          return;
        }
      } catch (IOException ignore) {
        break;
      }
    } while (true);
  }

  enum Status {
    DEFAULT,
    STARTING,
    STARTED,
    STOPPING,
  }
}
