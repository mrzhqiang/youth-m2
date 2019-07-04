package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 服务配置。
 *
 * @author qiang.zhang
 */
public class ServerConfig extends ProgramConfig {
  public final IntegerProperty serverPort = new SimpleIntegerProperty(0);

  @Override
  public void onLoad(Config config) {
    super.onLoad(config);
    if (config.hasPath("serverPort")) {
      serverPort.setValue(config.getInt("serverPort"));
    }
  }

  public final int getServerPort() {
    return serverPort.getValue();
  }

  @Override public ObjectNode objectNode() {
    return super.objectNode().put("serverPort", serverPort.getValue());
  }
}
