package youthm2.bootstrap;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import youthm2.bootstrap.backup.BackupData;
import youthm2.bootstrap.backup.BackupDataModel;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件备份管理器。
 *
 * @author mrzhqiang
 */
final class BackupManager {
    boolean enabled = false;

    private final BackupDataModel dataModel = new BackupDataModel();

    void start() {

    }

    void stop() {

    }

    void load(File backupFile) {
        Preconditions.checkNotNull(backupFile, "backup file == null");
        Config config = ConfigFactory.parseFile(backupFile);
        if (config.hasPath("dataList")) {
            List<String> backupList = config.getStringList("dataList");
            List<BackupData> dataList = backupList.stream()
                    .filter(s -> !Strings.isNullOrEmpty(s) && config.hasPath(s))
                    .map(config::getConfig)
                    .map(this::toBackupData)
                    .filter(data -> !Strings.isNullOrEmpty(data.source) && !Strings.isNullOrEmpty(data.sink))
                    .collect(Collectors.toList());
            dataModel.update(dataList);
        }
    }

    void updateModel(JTable dataTable) {
        dataTable.setModel(dataModel);
    }

    private BackupData toBackupData(Config config) {
        Preconditions.checkNotNull(config, "config == null");
        BackupData data = new BackupData();
        data.source = config.getString("source");
        data.sink = config.getString("sink");
        data.hour = config.getInt("hour");
        data.min = config.getInt("min");
        data.backupMode = config.getInt("backupMode");
        data.backupEnabled = config.getBoolean("backupEnabled");
        data.zipEnabled = config.getBoolean("zipEnabled");
        return data;
    }

    void zipChange(boolean enabled) {

    }

    void autoRunChange(boolean enabled) {

    }
}
