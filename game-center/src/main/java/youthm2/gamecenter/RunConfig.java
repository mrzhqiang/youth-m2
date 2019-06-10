package youthm2.gamecenter;

import javax.annotation.Nonnull;

/**
 * 运行网关配置。
 * <p>
 * 对应 Config.ini 中的 [RunGate] 部分。
 *
 * @author mrzhqiang
 */
public final class RunConfig extends ServerConfig {
  public static RunConfig newInstant() {
    return new RunConfig();
  }

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Override protected int defaultFormX() {
    return 437;
  }

  @Override protected int defaultFormY() {
    return 373;
  }

  @Override protected int defaultPort() {
    return 7200;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应 jar 文件名
    return "RunGate.exe";
  }

  @Nonnull @Override public String sectionName() {
    return "RunGate";
  }
}
