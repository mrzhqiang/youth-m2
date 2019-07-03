package bootstrap.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import bootstrap.model.backup.BackupData;

/**
 * 文件备份管理器。
 *
 * @author mrzhqiang
 */
final class BackupModel {
  private static final String BACKUP_FILE = "backup.json";

  final ObservableList<BackupData> dataList = FXCollections.observableArrayList();

  private final File backupFile = new File(BACKUP_FILE);

  void start() {

  }

  void stop() {

  }

  void loadConfig() {
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
}
