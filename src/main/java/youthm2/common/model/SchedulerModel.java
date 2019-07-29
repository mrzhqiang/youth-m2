package youthm2.common.model;

import javafx.application.Platform;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * SchedulerModel
 *
 * @author qiang.zhang
 */
public final class SchedulerModel {
  private SchedulerModel() {
  }

  private enum Holder {
    INSTANCE;
    private final Scheduler main = Schedulers.from(Platform::runLater);
  }

  public static Scheduler main() {
    return Holder.INSTANCE.main;
  }
}
