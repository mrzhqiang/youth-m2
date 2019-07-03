package bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 公开服务配置。
 *
 * @author qiang.zhang
 */
public final class PublicServerConfig extends ServerConfig {
  public final IntegerProperty publicPort = new SimpleIntegerProperty(0);

  @Override
  public void onLoad(Config config) {
    super.onLoad(config);
    if (config.hasPath("publicPort")) {
      publicPort.setValue(config.getInt("publicPort"));
    }
  }

  public int getPublicPort() {
    return publicPort.getValue();
  }

  @Override public ObjectNode objectNode() {
    return super.objectNode().put("publicPort", publicPort.getValue());
  }
}
