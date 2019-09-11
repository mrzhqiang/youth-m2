package youthm2.common;

import youthm2.common.internal.SimpleMonitor;
import youthm2.common.util.Environments;

/**
 * 监视器。
 * <p>
 * 这是一个简单的监视器，主要用来打印时间戳信息，看看哪些步骤花费的时间较长，以提供优化方向。
 *
 * @author mrzhqiang
 */
public interface Monitor {
  /**
   * 返回监视器的具体实现。
   * <p>
   * 注意，这个方法每次调用都返回一个新的实例，并且需要在 IDEA 中以调试模式运行，才会真正去打印时间戳。
   */
  static Monitor getInstance() {
    if (Environments.debugMode()) {
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
