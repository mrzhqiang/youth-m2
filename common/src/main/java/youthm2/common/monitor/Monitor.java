package youthm2.common.monitor;

/**
 * 监视器。
 */
public interface Monitor {
  /**
   * 返回监视器的具体实现。
   */
  static Monitor getInstance() {
    // 当前仅在 IDEA 中调试，因此只判断 IDEA 调试模式下的系统参数
    if (Boolean.parseBoolean(System.getProperty("intellij.debug.agent"))) {
      // 调试模式下，才具备监视器的真正功能。
      return new SimpleMonitor();
    }
    return EMPTY;
  }

  /**
   * 记录相应操作的名字，以及这一瞬间的时间戳，以备汇报时打印信息。
   */
  void record(String name);

  /**
   * 汇报最终的结果。
   * <p>
   * 这将会把所有记录包括起始时间戳和最终时间戳打印出来。
   */
  void report(String name);

  /**
   * 空实现是为生产模式使用，这将保证生产模式下的性能。
   */
  Monitor EMPTY = new Monitor() {
    @Override public void record(String name) {
      // do nothing
    }

    @Override public void report(String name) {
      // do nothing
    }
  };
}
