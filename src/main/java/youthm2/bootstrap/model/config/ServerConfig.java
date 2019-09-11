package youthm2.bootstrap.model.config;

import com.typesafe.config.Config;

/**
 * 服务配置。
 *
 * @author qiang.zhang
 */
public class ServerConfig extends ProgramConfig {
  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_SERVER_PORT = "serverPort";

  public Integer port;
  public Integer serverPort;

  @Override public void update(Config config) {
    super.update(config);
    if (config.hasPath(CONFIG_PORT)) {
      port = config.getInt(CONFIG_PORT);
    }
    if (config.hasPath(CONFIG_SERVER_PORT)) {
      serverPort = config.getInt(CONFIG_SERVER_PORT);
    }
  }
}
