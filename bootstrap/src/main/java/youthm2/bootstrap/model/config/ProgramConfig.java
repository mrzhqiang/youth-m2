package youthm2.bootstrap.model.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * 程序配置。
 *
 * @author qiang.zhang
 */
public class ProgramConfig {
  static final String CONFIG_ENABLED = "enabled";
  static final String CONFIG_X = "x";
  static final String CONFIG_Y = "y";
  static final String CONFIG_PATH = "path";

  public int x;
  public int y;
  public boolean enabled;
  public String path;

  static ProgramConfig of(Config program) {
    Preconditions.checkNotNull(program, "program config == null");
    ProgramConfig config = new ProgramConfig();
    config.x = program.getInt(CONFIG_X);
    config.y = program.getInt(CONFIG_Y);
    config.enabled = program.getBoolean(CONFIG_ENABLED);
    config.path = program.getString(CONFIG_PATH);
    return config;
  }
}
