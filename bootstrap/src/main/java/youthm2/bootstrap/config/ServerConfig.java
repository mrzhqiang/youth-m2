package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * @author mrzhqiang
 */
public class ServerConfig extends FormConfig {
  private static final String SERVER_PORT = "server.port";
  private static final String SERVER_INTERVAL_PORT = "server.interval.port";
  private static final String SERVER_MON_PORT = "server.mon.port";

  private int port;
  private int intervalPort;
  private int monPort;

  @Override public void load(Config config) {
    super.load(config);
    port = config.getInt(SERVER_PORT);
    if (config.hasPath(SERVER_INTERVAL_PORT)) {
      intervalPort = config.getInt(SERVER_INTERVAL_PORT);
    }
    if (config.hasPath(SERVER_MON_PORT)) {
      monPort = config.getInt(SERVER_MON_PORT);
    }
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getIntervalPort() {
    return intervalPort;
  }

  public void setIntervalPort(int intervalPort) {
    this.intervalPort = intervalPort;
  }

  public int getMonPort() {
    return monPort;
  }

  public void setMonPort(int monPort) {
    this.monPort = monPort;
  }
}
