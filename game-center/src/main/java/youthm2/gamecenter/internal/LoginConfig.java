package youthm2.gamecenter.internal;

import javax.annotation.Nonnull;

/**
 * 登陆网关配置。
 * <p>
 * 对应 Config.ini 中的 [LoginGate] 部分。
 *
 * @author mrzhqiang
 */
public final class LoginConfig extends ServerConfig {
  public static LoginConfig newInstant() {
    return new LoginConfig();
  }

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Override protected int defaultFormX() {
    return 0;
  }

  @Override protected int defaultFormY() {
    return 0;
  }

  @Override protected int defaultPort() {
    return 7000;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应的 jar 文件名
    return "LoginGate.exe";
  }

  @Nonnull @Override public String sectionName() {
    return "LoginGate";
  }
}
