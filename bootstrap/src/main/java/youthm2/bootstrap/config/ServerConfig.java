package youthm2.bootstrap.config;

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
}
