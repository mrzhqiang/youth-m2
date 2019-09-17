package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import java.nio.file.Paths;

/**
 * 程序配置。
 *
 * @author qiang.zhang
 */
public class ProgramConfig {
  private static final String CONFIG_ENABLED = "enabled";
  private static final String CONFIG_X = "x";
  private static final String CONFIG_Y = "y";
  private static final String CONFIG_PATH = "path";

  public Integer x;
  public Integer y;
  public Boolean enabled;
  public String path;

  public void update(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    x = config.getInt(CONFIG_X);
    y = config.getInt(CONFIG_Y);
    enabled = config.getBoolean(CONFIG_ENABLED);
    path = Paths.get(config.getString(CONFIG_PATH)).toString();
  }
}
