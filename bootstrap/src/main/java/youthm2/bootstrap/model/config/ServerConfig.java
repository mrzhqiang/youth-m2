package youthm2.bootstrap.model.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * 服务配置。
 *
 * @author qiang.zhang
 */
public class ServerConfig extends ProgramConfig {
  static final String CONFIG_PORT = "port";
  static final String CONFIG_SERVER_PORT = "serverPort";

  public int port;
  public int serverPort;

  static ServerConfig of(Config server) {
    Preconditions.checkNotNull(server, "server config == null");
    ServerConfig config = new ServerConfig();
    config.x = server.getInt(CONFIG_X);
    config.y = server.getInt(CONFIG_Y);
    config.port = server.getInt(CONFIG_PORT);
    config.serverPort = server.getInt(CONFIG_SERVER_PORT);
    config.enabled = server.getBoolean(CONFIG_ENABLED);
    config.path = server.getString(CONFIG_PATH);
    return config;
  }
}
