package youthm2.common.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;

/**
 * ConfigModel
 *
 * @author qiang.zhang
 */
public final class ConfigModel {
  private final File configFile;
  private final Config config;

  public ConfigModel(File configFile) {
    Preconditions.checkNotNull(configFile, "config file == null");
    Preconditions.checkState(configFile.isFile(), "config file is not file");
    Preconditions.checkState(configFile.exists(), "config file is not exists");
    this.configFile = configFile;
    this.config = ConfigFactory.parseFile(configFile);
  }

  public Config getConfig() {
    return config;
  }

  public Config withDefaultConfig() {
    return config.withFallback(ConfigFactory.load());
  }

  public void save() {

  }

  public static void main(String[] args) {
    Config load = ConfigFactory.load();
  }
}
