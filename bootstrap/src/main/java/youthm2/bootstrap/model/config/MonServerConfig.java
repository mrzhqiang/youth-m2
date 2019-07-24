package youthm2.bootstrap.model.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * 通用服务配置。
 *
 * @author qiang.zhang
 */
public final class MonServerConfig extends ServerConfig {
  private static final String CONFIG_PUBLIC_PORT = "monPort";
  public int monPort;

  static MonServerConfig of(Config monServer) {
    Preconditions.checkNotNull(monServer, "mon server config == null");
    MonServerConfig config = new MonServerConfig();
    config.x = monServer.getInt(CONFIG_X);
    config.y = monServer.getInt(CONFIG_Y);
    config.port = monServer.getInt(CONFIG_PORT);
    config.serverPort = monServer.getInt(CONFIG_SERVER_PORT);
    config.monPort = monServer.getInt(CONFIG_PUBLIC_PORT);
    config.enabled = monServer.getBoolean(CONFIG_ENABLED);
    config.path = monServer.getString(CONFIG_PATH);
    return config;
  }
}
