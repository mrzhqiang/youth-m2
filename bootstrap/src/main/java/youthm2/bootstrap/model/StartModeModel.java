package youthm2.bootstrap.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * StartModeModel
 *
 * @author qiang.zhang
 */
public final class StartModeModel {
  private StartModeModel() {
    throw new AssertionError("No instance");
  }

  public static LocalDateTime normal() {
    return LocalDateTime.now();
  }

  public static LocalDateTime delay(int hours, int minutes) {
    return LocalDateTime.now().plusHours(hours).plusMinutes(minutes);
  }

  public static LocalDateTime timing(int hours, int minutes) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime timing = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hours, minutes));
    // 如果现在已经超过定时，那么给定时加个鸡腿（加一天）
    if (now.isAfter(timing)) {
      timing = timing.plusDays(1);
    }
    return timing;
  }
}
