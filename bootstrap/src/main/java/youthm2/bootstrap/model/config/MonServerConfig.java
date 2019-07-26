package youthm2.bootstrap.model.config;

import com.typesafe.config.Config;

/**
 * 通用服务配置。
 *
 * @author qiang.zhang
 */
public final class MonServerConfig extends ServerConfig {
  private static final String CONFIG_PUBLIC_PORT = "monPort";

  public Integer monPort;

  @Override public void update(Config config) {
    super.update(config);
    monPort = config.getInt(CONFIG_PUBLIC_PORT);
  }
}
