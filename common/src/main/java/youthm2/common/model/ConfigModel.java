package youthm2.common.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;

/**
 * 配置模块。
 * <p>
 * 通过指定配置文件加载，如果文件不存在，那么加载类路径上的默认配置。
 * <p>
 * 所包含的逻辑是：
 * 1. 环境变量配置，从系统环境变量中加载。
 * 2. 自定义配置，从外部传入的 json 文件。
 * 3. 默认配置，位于资源目录的 reference.conf 文件。
 *
 * @author qiang.zhang
 */
public final class ConfigModel {
  private ConfigModel() {
    throw new AssertionError("No instance");
  }

  public static Config load(File configFile) {
    Preconditions.checkNotNull(configFile, "config file == null");
    Preconditions.checkState(configFile.isFile(), "config file is not file");
    Preconditions.checkState(configFile.exists(), "config file is not exists");
    return ConfigFactory.parseFile(configFile);
  }

  public static Config loadDefault() {
    return ConfigFactory.load();
  }

  public static Config merge(Config primary, Config second) {
    Preconditions.checkNotNull(primary, "primary config == null");
    Preconditions.checkNotNull(second, "second config == nul");
    return primary.withFallback(second);
  }

  public static Config mergeDefault(Config primary) {
    Preconditions.checkNotNull(primary, "primary config == null");
    return primary.withFallback(loadDefault());
  }

  public static void saveConfig(File configFile, String content) {
    Preconditions.checkNotNull(configFile, "config file == null");
    Preconditions.checkState(configFile.isFile(), "config file is not file");
    Preconditions.checkState(configFile.exists(), "config file is not exists");
    Preconditions.checkNotNull(content, "content == null");
    FileModel.createOrExists(configFile);
    FileModel.onceWrite(configFile, content);
  }
}
