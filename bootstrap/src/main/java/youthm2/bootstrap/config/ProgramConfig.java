package youthm2.bootstrap.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import youthm2.common.Json;

/**
 * 程序配置。
 *
 * @author qiang.zhang
 */
public class ProgramConfig {
  public final IntegerProperty x = new SimpleIntegerProperty(0);
  public final IntegerProperty y = new SimpleIntegerProperty(0);
  public final IntegerProperty port = new SimpleIntegerProperty(0);
  public final BooleanProperty enabled = new SimpleBooleanProperty(false);
  public final StringProperty filename = new SimpleStringProperty("");

  public void onLoad(Config config) {
    if (config.hasPath("x")) {
      x.setValue(config.getInt("x"));
    }
    if (config.hasPath("y")) {
      y.setValue(config.getInt("y"));
    }
    if (config.hasPath("port")) {
      port.setValue(config.getInt("port"));
    }
    if (config.hasPath("enabled")) {
      enabled.setValue(config.getBoolean("enabled"));
    }
    if (config.hasPath("filename")) {
      filename.setValue(config.getString("filename"));
    }
  }

  public ObjectNode objectNode() {
    return Json.newObject()
        .put("x", x.getValue())
        .put("y", y.getValue())
        .put("port", port.getValue())
        .put("enabled", enabled.getValue())
        .put("filename", filename.getValue());
  }
}
