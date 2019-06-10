package youthm2.gamecenter;

import javax.annotation.Nonnull;

/**
 * 日志配置。
 * <p>
 * 对应 Config.ini 中的 [LogServer] 部分。
 *
 * @author mrzhqiang
 */
public final class LogConfig extends ServerConfig {
  public static LogConfig newInstant() {
    return new LogConfig();
  }

  @Nonnull @Override public String sectionName() {
    return "LogServer";
  }

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Override protected int defaultFormX() {
    return 252;
  }

  @Override protected int defaultFormY() {
    return 286;
  }

  @Override protected int defaultPort() {
    return 10000;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应的 jar 文件名
    return "LogDataServer.exe";
  }
}
