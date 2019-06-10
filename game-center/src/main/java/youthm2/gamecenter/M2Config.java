package youthm2.gamecenter;

import javax.annotation.Nonnull;
import org.ini4j.Profile;

/**
 * 游戏主程序配置。
 * <p>
 * 对应 Config.ini 中的 [M2Server] 部分。
 *
 * @author mrzhqiang
 */
public final class M2Config extends ServerConfig {
  private static final String OPTION_MSG_SRV_PORT = "MsgSrvPort";

  private static final int DEFAULT_MSG_SRV_PORT = 4900;

  public static M2Config newInstant() {
    return new M2Config();
  }

  public int msgServerPort = DEFAULT_MSG_SRV_PORT;

  @Override protected boolean defaultEnabled() {
    return true;
  }

  @Override protected int defaultFormX() {
    return 561;
  }

  @Override protected int defaultFormY() {
    return 0;
  }

  @Override protected int defaultPort() {
    return 5000;
  }

  @Nonnull @Override protected String defaultFile() {
    return "M2Server.exe";
  }

  @Nonnull @Override public String sectionName() {
    return "M2Server";
  }

  @Override public void readFrom(@Nonnull Profile.Section section) {
    super.readFrom(section);
    msgServerPort = section.get(OPTION_MSG_SRV_PORT, Integer.class, DEFAULT_MSG_SRV_PORT);
  }

  @Override public void writeTo(@Nonnull Profile.Section section) {
    super.writeTo(section);
    section.put(OPTION_MSG_SRV_PORT, msgServerPort);
  }
}
