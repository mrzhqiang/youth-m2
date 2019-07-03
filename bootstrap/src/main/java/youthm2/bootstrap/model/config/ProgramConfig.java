package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import java.nio.file.Paths;
import java.util.Locale;
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
  public final StringProperty path = new SimpleStringProperty("");
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
    if (config.hasPath("path")) {
      path.setValue(config.getString("path"));
    }
    if (config.hasPath("filename")) {
      filename.setValue(config.getString("filename"));
    }
  }

  public final int getX() {
    return x.getValue();
  }

  public final int getY() {
    return y.getValue();
  }

  public final int getPort() {
    return port.getValue();
  }

  public final boolean isEnabled() {
    return enabled.getValue();
  }

  public final String getPath() {
    return path.getValue();
  }

  public final String getFilename() {
    return filename.getValue();
  }

  public ObjectNode objectNode() {
    return Json.newObject()
        .put("x", x.getValue())
        .put("y", y.getValue())
        .put("port", port.getValue())
        .put("enabled", enabled.getValue())
        .put("filename", filename.getValue());
  }

  final String command(String home) {
    Preconditions.checkNotNull(home, "home == null");
    String link = Paths.get(home, path.getValue(), filename.getValue()).toAbsolutePath().toString();
    return String.format(Locale.getDefault(),
        "%s %d %d %d",
        link, port.getValue(), x.getValue(), y.getValue());
  }
}
