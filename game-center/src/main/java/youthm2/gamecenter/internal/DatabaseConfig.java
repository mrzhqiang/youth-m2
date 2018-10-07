package youthm2.gamecenter.internal;

import javax.annotation.Nonnull;
import org.ini4j.Profile;

/**
 * 数据库配置。
 * <p>
 * 对应 Config.ini 中的 [DBServer] 部分。
 *
 * @author mrzhqiang
 */
public final class DatabaseConfig extends ServerConfig {
  private static final String OPTION_SERVER_PORT = "ServerPort";

  private static final int DEFAULT_SERVER_PORT = 6000;

  public static DatabaseConfig newInstant() {
    return new DatabaseConfig();
  }

  public int serverPort = DEFAULT_SERVER_PORT;

  @Nonnull @Override public String sectionName() {
    return "DBServer";
  }

  @Override protected int defaultFormX() {
    return 0;
  }

  @Override protected int defaultFormY() {
    return 373;
  }

  @Override protected int defaultPort() {
    return 5100;
  }

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Nonnull @Override protected String defaultFile() {
    // FIXME: 2018/10/7 修改为对应的 jar 文件名
    return "DBServer.exe";
  }

  @Override public void readFrom(@Nonnull Profile.Section section) {
    super.readFrom(section);
    serverPort = section.get(OPTION_SERVER_PORT, Integer.class, DEFAULT_SERVER_PORT);
  }

  @Override public void writeTo(@Nonnull Profile.Section section) {
    super.writeTo(section);
    section.put(OPTION_SERVER_PORT, serverPort);
  }
}
