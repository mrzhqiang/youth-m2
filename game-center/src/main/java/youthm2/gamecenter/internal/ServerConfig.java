package youthm2.gamecenter.internal;

import javax.annotation.Nonnull;
import org.ini4j.Profile;
import youthm2.gamecenter.IConfig;

/**
 * 服务器配置。
 * <p>
 * 对应 Config.ini 中的各个服务器配置，这里只包含最基础的属性字段。
 *
 * @author mrzhqiang
 */
abstract class ServerConfig implements IConfig {
  private static final String OPTION_FORM_X = "MainFormX";
  private static final String OPTION_FORM_Y = "MainFormY";
  private static final String OPTION_PORT = "GatePort";
  private static final String OPTION_ENABLED = "GetStart";
  private static final String OPTION_FILE = "programFile";

  public boolean enabled;
  public int formX;
  public int formY;
  /** 网关端口。 */
  public int port;
  public String file;

  abstract protected boolean defaultEnabled();

  abstract protected int defaultFormX();

  abstract protected int defaultFormY();

  abstract protected int defaultPort();

  @Nonnull
  abstract protected String defaultFile();

  @Override public void readFrom(@Nonnull Profile.Section section) {
    enabled = Util.getBoolean(section, OPTION_ENABLED, defaultEnabled());
    formX = section.get(OPTION_FORM_X, Integer.class, defaultFormX());
    formY = section.get(OPTION_FORM_Y, Integer.class, defaultFormY());
    port = section.get(OPTION_PORT, Integer.class, defaultPort());
    file = section.get(OPTION_FILE, defaultFile());
  }

  @Override public void writeTo(@Nonnull Profile.Section section) {
    section.put(OPTION_FORM_X, formX);
    section.put(OPTION_FORM_Y, formY);
    section.put(OPTION_PORT, port);
    Util.putBoolean(section, OPTION_ENABLED, enabled);
    section.put(OPTION_FILE, file);
  }
}
