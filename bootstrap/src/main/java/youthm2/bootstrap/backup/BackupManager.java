package youthm2.bootstrap.backup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.swing.*;
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

    private final BackupDataModel dataModel = new BackupDataModel();

    public void start() {

    }

    public void stop() {

    }

    public void load(File backupFile) {
        Preconditions.checkNotNull(backupFile, "backup file == null");
        Config config = ConfigFactory.parseFile(backupFile);
        if (config.hasPath("dataList")) {
            List<String> backupList = config.getStringList("dataList");
            List<BackupData> dataList = backupList.stream()
                    .filter(s -> !Strings.isNullOrEmpty(s) && config.hasPath(s))
                    .map(config::getConfig)
                    .map(this::ofBackupData)
                    .filter(data -> !Strings.isNullOrEmpty(data.source) && !Strings.isNullOrEmpty(data.sink))
                    .collect(Collectors.toList());
            dataModel.update(dataList);
        }
    }

    public void updateModel(JTable dataTable) {
        dataTable.setModel(dataModel);
    }

    private BackupData ofBackupData(Config config) {
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
}
