package youthm2.gamecenter;

import javax.annotation.Nonnull;

/**
 * 角色网关配置。
 * <p>
 * 对应 Config.ini 中的 [SelGate] 部分。
 *
 * @author mrzhqiang
 */
public final class SelConfig extends ServerConfig {
  public static SelConfig newInstant() {
    return new SelConfig();
  }

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Override protected int defaultFormX() {
    return 0;
  }

  @Override protected int defaultFormY() {
    return 180;
  }

  @Override protected int defaultPort() {
    return 7100;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应 jar 文件名
    return "SelGate.exe";
  }

  @Nonnull @Override public String sectionName() {
    return "SelGate";
  }
}
