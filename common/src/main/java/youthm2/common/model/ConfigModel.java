package youthm2.common.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOGGER = LoggerFactory.getLogger("common");

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

  public Config defaultConfig() {
    return ConfigFactory.load();
  }

  public Config mergeConfig() {
    return getConfig().withFallback(defaultConfig());
  }

  public void saveConfig() {
    if (!configFile.exists()) {
      try {
        boolean newFile = configFile.createNewFile();
        LOGGER.info("创建新的配置文件: {}", newFile);
      } catch (Exception e) {
        LOGGER.error("创建新的配置文件出错：{}", e.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR);
        //dialog.setHeaderText("无法创建新的配置文件");
        //dialog.show();
        return;
      }
    }
    //try (FileWriter writer = new FileWriter(configFile)) {
    //  writer.write(Json.prettyPrint(config.toJsonNode()));
    //  writer.flush();
    //} catch (Exception e) {
    //  LOGGER.error("保存当前配置出错：{}", e.getMessage());
    //  ExceptionDialog dialog = new ExceptionDialog(e);
    //  dialog.setHeaderText("无法保存当前配置");
    //  dialog.show();
    //}
  }

  public static void main(String[] args) {
    Config load = ConfigFactory.load();
  }
}
