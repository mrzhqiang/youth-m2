package youthm2.common;

import javax.annotation.Nonnull;

/**
 * 平台。
 * <p>
 * 根据平台不同，对文件和路径进行不同的符号转义。
 *
 * @author qiang.zhang
 */
public enum Platform {
  WINDOWS('\\', ';'), UNIX('/', ':');

  public final char fileSeparator;
  public final char pathSeparator;

  Platform(char fileSeparator, char pathSeparator) {
    this.fileSeparator = fileSeparator;
    this.pathSeparator = pathSeparator;
  }

  @Nonnull
  public static Platform current() {
    return SystemInfo.isWindows ? WINDOWS : UNIX;
  }
}
