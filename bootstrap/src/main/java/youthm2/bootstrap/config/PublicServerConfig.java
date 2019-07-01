package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 公开服务配置。
 *
 * @author qiang.zhang
 */
public final class PublicServerConfig extends ServerConfig {
  public int publicPort;

  @Override
  public void onLoad(Config config) {
    super.onLoad(config);
    if (config.hasPath("publicPort")) {
      publicPort = config.getInt("publicPort");
    }
  }
}
