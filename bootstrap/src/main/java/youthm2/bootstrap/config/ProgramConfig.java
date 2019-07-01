package youthm2.bootstrap.config;

import com.typesafe.config.Config;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import youthm2.common.Loader;

/**
 * 程序配置。
 *
 * @author qiang.zhang
 */
public class ProgramConfig implements Loader {
  public int x;
  public int y;
  public int port;
  public BooleanProperty enabled = new SimpleBooleanProperty(false);
  public String filename;

  @Override
  public void onLoad(Config config) {
    if (config.hasPath("x")) {
      x = config.getInt("x");
    }
    if (config.hasPath("y")) {
      y = config.getInt("y");
    }
    if (config.hasPath("port")) {
      port = config.getInt("port");
    }
    if (config.hasPath("enabled")) {
      enabled.setValue(config.getBoolean("enabled"));
    }
    if (config.hasPath("filename")) {
      filename = config.getString("filename");
    }
  }
}
