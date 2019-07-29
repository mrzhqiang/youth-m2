package youthm2.common.monitor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import youthm2.common.Monitor;

/**
 * 最简单的监视器。
 *
 * @author mrzhqiang
 */
public final class SimpleMonitor implements Monitor {
  private static final Logger LOGGER = LoggerFactory.getLogger("monitor");

  private final long startTime = System.currentTimeMillis();
  private final List<Cycle> cycleList = Lists.newArrayList();

  @Override
  public void record(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return;
    }
    cycleList.add(new Cycle(name, System.currentTimeMillis()));
  }

  @Override
  public void report(String name) {
    long entTime = System.currentTimeMillis();
    long totalTime = entTime - startTime;
    LOGGER.info("The [{}] total time: {}(ms)", name, totalTime);

    for (int i = 0; i < cycleList.size(); i++) {
      Cycle book = cycleList.get(i);
      long intervalTime;
      if (i == 0) {
        intervalTime = book.timestamp - startTime;
      } else if (i == cycleList.size() - 1) {
        intervalTime = entTime - book.timestamp;
      } else {
        intervalTime = cycleList.get(i + 1).timestamp - book.timestamp;
      }
      LOGGER.info("The [{}] >>> [{}] time: {}(ms)", name, book.name, intervalTime);
    }
    cycleList.clear();
  }

  private static final class Cycle {
    final String name;
    final long timestamp;

    private Cycle(String name, long timestamp) {
      this.name = name;
      this.timestamp = timestamp;
    }
  }
}
