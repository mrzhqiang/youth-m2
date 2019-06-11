package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * @author mrzhqiang
 */
public final class GateConfig extends FormConfig {
  private static final String GATE_PORT = "gate.port";

  private int port;

  @Override public void load(Config config) {
    super.load(config);
    port = config.getInt(GATE_PORT);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
