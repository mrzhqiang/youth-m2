package youthm2.gamecenter.internal;

import javax.annotation.Nonnull;

/**
 * 排行榜插件配置。
 * <p>
 * 对应 Config.ini 中的 [PlugTop] 部分。
 *
 * @author mrzhqiang
 */
public final class PlugConfig extends ServerConfig {
  public static PlugConfig newInstant() {
    return new PlugConfig();
  }

  @Override protected boolean defaultEnabled() {
    return false;
  }

  @Override protected int defaultFormX() {
    return 200;
  }

  @Override protected int defaultFormY() {
    return 0;
  }

  @Override protected int defaultPort() {
    return 0;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应的 jar 文件名
    return "PlugTop.exe";
  }

  @Nonnull @Override public String sectionName() {
    return "PlugTop";
  }
}
