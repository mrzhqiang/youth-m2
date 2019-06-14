package youthm2.bootstrap.backup;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件备份管理器。
 *
 * @author mrzhqiang
 */
public final class BackupManager {
  public boolean enabled = false;
  public final BackupDataModel dataModel;

  public BackupManager() {
    dataModel = new BackupDataModel();
  }

  public void start() {

  }

  public void stop() {

  }

  public void load(File file) {
    Config config = ConfigFactory.parseFile(file);
    if (config.hasPath("dataList")) {
      List<String> backupList = config.getStringList("dataList");
      List<BackupData> dataList = backupList.stream()
              .filter(s -> !Strings.isNullOrEmpty(s))
              .map(config::getConfig)
              .map(BackupData::of)
              .filter(data ->
                      !Strings.isNullOrEmpty(data.source) && !Strings.isNullOrEmpty(data.destination))
              .collect(Collectors.toList());
      dataModel.update(dataList);
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
