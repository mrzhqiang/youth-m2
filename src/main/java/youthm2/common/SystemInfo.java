package youthm2.common;

import java.util.Locale;

/**
 * SystemInfo
 *
 * @author qiang.zhang
 */
public final class SystemInfo {
  private SystemInfo() {
    throw new AssertionError("This is factory class");
  }

  public static final String OS_NAME = System.getProperty("os.name");
  public static final String OS_VERSION =
      System.getProperty("os.version").toLowerCase(Locale.ENGLISH);

  private static final String _OS_NAME = OS_NAME.toLowerCase(Locale.ENGLISH);
  public static final boolean isWindows = _OS_NAME.startsWith("windows");
  public static final boolean isMac = _OS_NAME.startsWith("mac");
  public static final boolean isLinux = _OS_NAME.startsWith("linux");

  public static final String JAVA_VERSION = System.getProperty("java.version");
  public static final String JAVA_RUNTIME_VERSION = getRuntimeVersion(JAVA_VERSION);

  private static String getRuntimeVersion(@SuppressWarnings("SameParameterValue") String fallback) {
    String rtVersion = System.getProperty("java.runtime.version");
    return Character.isDigit(rtVersion.charAt(0)) ? rtVersion : fallback;
  }
}
