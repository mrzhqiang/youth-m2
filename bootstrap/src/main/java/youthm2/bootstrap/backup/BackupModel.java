package youthm2.bootstrap.backup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 文件备份管理器。
 *
 * @author mrzhqiang
 */
public final class BackupModel {
  public final ObservableList<BackupData> dataList = FXCollections.observableArrayList();

  public void start() {

  }

  public void stop() {

  }

  public void load(File backupFile) {
    Preconditions.checkNotNull(backupFile, "backup file == null");
    Config config = ConfigFactory.parseFile(backupFile);
    if (config.hasPath("dataList")) {
      List<String> backupList = config.getStringList("dataList");
      dataList.addAll(backupList.stream()
          .filter(s -> !Strings.isNullOrEmpty(s) && config.hasPath(s))
          .map(config::getConfig)
          .map(this::toBackupData)
          .collect(Collectors.toList()));
    }
  }

  private BackupData toBackupData(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    BackupData data = new BackupData();
    data.source.setValue(config.getString("source"));
    data.sink.setValue(config.getString("sink"));
    data.hour.setValue(config.getInt("hour"));
    data.min.setValue(config.getInt("min"));
    data.backupMode.setValue(config.getInt("backupMode"));
    data.backupEnabled.setValue(config.getBoolean("backupEnabled"));
    data.zipEnabled.setValue(config.getBoolean("zipEnabled"));
    return data;
  }

  public void zipChange(boolean enabled) {

  }

  public void autoRunChange(boolean enabled) {

  }
}
