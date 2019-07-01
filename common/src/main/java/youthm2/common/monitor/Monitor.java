package youthm2.common.monitor;

public interface Monitor {
  static Monitor getInstance() {
    // 当前仅在 IDEA 中调试，因此只判断 IDEA 调试模式下的系统参数
    if (Boolean.parseBoolean(System.getProperty("intellij.debug.agent"))) {
      return new SimpleMonitor();
    }
    return EMPTY;
  }

  void record(String name);

  void report(String name);

  Monitor EMPTY = new Monitor() {
    @Override public void record(String name) {
      // do nothing
    }

    @Override public void report(String name) {
      // do nothing
    }
  };
}
