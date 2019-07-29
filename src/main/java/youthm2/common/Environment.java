package youthm2.common;

/**
 * 环境工具。
 *
 * @author qiang.zhang
 */
public final class Environment {
  private static final String DEBUG_DIR = "sample";

  private Environment() {
    throw new AssertionError("This is factory class");
  }

  public static boolean isDebug() {
    // 只喜欢 IDEA，就是这么任性
    return Boolean.parseBoolean(System.getProperty("intellij.debug.agent"));
  }

  public static String workDirectory() {
    return System.getProperty("user.dir");
  }

  public static String debugDirectory() {
    return DEBUG_DIR;
  }
}
