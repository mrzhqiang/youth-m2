package youthm2.bootstrap.config;

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
}
