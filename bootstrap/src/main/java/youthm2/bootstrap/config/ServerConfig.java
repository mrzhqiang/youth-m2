package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 服务配置。
 *
 * @author qiang.zhang
 */
public class ServerConfig extends ProgramConfig {
  public int serverPort;

  @Override
  public void onLoad(Config config) {
    super.onLoad(config);
    if (config.hasPath("serverPort")) {
      serverPort = config.getInt("serverPort");
    }
  }
}
