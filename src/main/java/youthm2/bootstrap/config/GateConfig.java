package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 网关配置。
 *
 * @author qiang.zhang
 */
public final class GateConfig extends ProgramConfig {
  private static final String CONFIG_PORT = "port";

  public Integer port;

  @Override public void update(Config config) {
    super.update(config);
    if (config.hasPath(CONFIG_PORT)) {
      port = config.getInt(CONFIG_PORT);
    }
  }
}
