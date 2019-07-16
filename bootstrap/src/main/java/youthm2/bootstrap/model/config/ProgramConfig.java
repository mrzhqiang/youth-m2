package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import youthm2.common.Json;

/**
 * 程序配置。
 *
 * @author qiang.zhang
 */
public class ProgramConfig {
  public final BooleanProperty enabled = new SimpleBooleanProperty(false);
  public final StringProperty x = new SimpleStringProperty("0");
  public final StringProperty y = new SimpleStringProperty("0");
  public final StringProperty port = new SimpleStringProperty("0");
  public final StringProperty path = new SimpleStringProperty("");

  public void onLoad(Config config) {
    if (config.hasPath("enabled")) {
      enabled.setValue(config.getBoolean("enabled"));
    }
    if (config.hasPath("x")) {
      x.setValue(String.valueOf(config.getInt("x")));
    }
    if (config.hasPath("y")) {
      y.setValue(String.valueOf(config.getInt("y")));
    }
    if (config.hasPath("port")) {
      port.setValue(String.valueOf(config.getInt("port")));
    }
    if (config.hasPath("path")) {
      path.setValue(config.getString("path"));
    }
  }

  public String getX() {
    return x.getValue();
  }

  public String getY() {
    return y.getValue();
  }

  public String getPort() {
    return port.getValue();
  }

  public boolean isEnabled() {
    return enabled.getValue();
  }

  public String getPath() {
    return path.get();
  }

  public ObjectNode objectNode() {
    return Json.newObject()
        .put("enabled", enabled.getValue())
        .put("x", x.getValue())
        .put("y", y.getValue())
        .put("port", port.getValue())
        .put("path", path.getValue());
  }
}
