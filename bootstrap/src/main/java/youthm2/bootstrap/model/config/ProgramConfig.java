package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  public final BooleanProperty enabled = new SimpleBooleanProperty(false);
  public final IntegerProperty x = new SimpleIntegerProperty(0);
  public final IntegerProperty y = new SimpleIntegerProperty(0);
  public final IntegerProperty port = new SimpleIntegerProperty(0);
  public final StringProperty path = new SimpleStringProperty("");
  public final StringProperty filename = new SimpleStringProperty("");

  public void onLoad(Config config) {
    if (config.hasPath("enabled")) {
      enabled.setValue(config.getBoolean("enabled"));
    }
    if (config.hasPath("x")) {
      x.setValue(config.getInt("x"));
    }
    if (config.hasPath("y")) {
      y.setValue(config.getInt("y"));
    }
    if (config.hasPath("port")) {
      port.setValue(config.getInt("port"));
    }
    if (config.hasPath("path")) {
      path.setValue(config.getString("path"));
    }
    if (config.hasPath("filename")) {
      filename.setValue(config.getString("filename"));
    }
  }

  public int getX() {
    return x.getValue();
  }

  public int getY() {
    return y.getValue();
  }

  public int getPort() {
    return port.getValue();
  }

  public boolean isEnabled() {
    return enabled.getValue();
  }

  public String getPath() {
    return path.get();
  }

  public String getFilename() {
    return filename.get();
  }

  public ObjectNode objectNode() {
    return Json.newObject()
        .put("enabled", enabled.getValue())
        .put("x", x.getValue())
        .put("y", y.getValue())
        .put("port", port.getValue())
        .put("path", path.getValue())
        .put("filename", filename.getValue());
  }

  public final Path link(String home) {
    Preconditions.checkNotNull(home, "home == null");
    String pathValue = path.getValue();
    String filenameValue = filename.getValue();
    return Paths.get(home, pathValue, filenameValue);
  }
}
