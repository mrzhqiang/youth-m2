package youthm2.gamecenter;

import javax.annotation.Nonnull;
import org.ini4j.Profile;

/**
 * 账户配置。
 * <p>
 * 实际上就是登陆服务器配置，对应 Config.ini 中的 [LoginSrv] 部分。
 *
 * @author mrzhqiang
 */
public final class AccountConfig extends ServerConfig {
  private static final String OPTION_SERVER_PORT = "ServerPort";
  private static final String OPTION_MON_PORT = "MonPort";

  private static final int DEFAULT_SERVER_PORT = 5600;
  private static final int DEFAULT_MON_PORT = 3000;

  public static AccountConfig newInstant() {
    return new AccountConfig();
  }

  public int serverPort = DEFAULT_SERVER_PORT;
  public int monPort = DEFAULT_MON_PORT;

  @Nonnull @Override public String sectionName() {
    return "LoginSrv";
  }

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Override protected int defaultFormX() {
    return 252;
  }

  @Override protected int defaultFormY() {
    return 0;
  }

  @Override protected int defaultPort() {
    return 5500;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应的 jar 文件名
    return "LoginSrv.exe";
  }

  @Override public void readFrom(@Nonnull Profile.Section section) {
    super.readFrom(section);
    serverPort = section.get(OPTION_SERVER_PORT, Integer.class, DEFAULT_SERVER_PORT);
    monPort = section.get(OPTION_MON_PORT, Integer.class, DEFAULT_MON_PORT);
  }

  @Override public void writeTo(@Nonnull Profile.Section section) {
    super.writeTo(section);
    section.put(OPTION_SERVER_PORT, serverPort);
    section.put(OPTION_MON_PORT, monPort);
  }
}
