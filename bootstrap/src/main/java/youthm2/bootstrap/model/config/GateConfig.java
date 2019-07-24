package youthm2.bootstrap.model.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * 网关配置。
 *
 * @author qiang.zhang
 */
public final class GateConfig extends ProgramConfig {
  private static final String CONFIG_PORT = "port";

  public int port;

  static GateConfig of(Config gate) {
    Preconditions.checkNotNull(gate, "gate config == null");
    GateConfig config = new GateConfig();
    config.x = gate.getInt(CONFIG_X);
    config.y = gate.getInt(CONFIG_Y);
    config.port = gate.getInt(CONFIG_PORT);
    config.enabled = gate.getBoolean(CONFIG_ENABLED);
    config.path = gate.getString(CONFIG_PATH);
    return config;
  }
}
