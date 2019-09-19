package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 通用服务配置。
 *
 * @author qiang.zhang
 */
public class MonServerConfig extends ServerConfig {
  private static final String CONFIG_PUBLIC_PORT = "monPort";

  public Integer monPort;

  @Override public void update(Config config) {
    super.update(config);
    if (config.hasPath(CONFIG_PUBLIC_PORT)) {
      monPort = config.getInt(CONFIG_PUBLIC_PORT);
    }
  }
}
