package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 服务配置。
 *
 * @author qiang.zhang
 */
public class ServerConfig extends ProgramConfig {
  public final StringProperty serverPort = new SimpleStringProperty("0");

  @Override
  public void onLoad(Config config) {
    super.onLoad(config);
    if (config.hasPath("serverPort")) {
      serverPort.setValue(String.valueOf(config.getInt("serverPort")));
    }
  }

  public String getServerPort() {
    return serverPort.getValue();
  }

  @Override public ObjectNode objectNode() {
    return super.objectNode().put("serverPort", serverPort.getValue());
  }
}
