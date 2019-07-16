package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 公开服务配置。
 *
 * @author qiang.zhang
 */
public final class PublicServerConfig extends ServerConfig {
  public final StringProperty publicPort = new SimpleStringProperty("0");

  @Override
  public void onLoad(Config config) {
    super.onLoad(config);
    if (config.hasPath("publicPort")) {
      publicPort.setValue(String.valueOf(config.getInt("publicPort")));
    }
  }

  public String getPublicPort() {
    return publicPort.getValue();
  }

  @Override public ObjectNode objectNode() {
    return super.objectNode().put("publicPort", publicPort.getValue());
  }
}
