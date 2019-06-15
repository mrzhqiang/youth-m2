package youthm2.bootstrap.backup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件备份管理器。
 *
 * @author mrzhqiang
 */
public final class BackupManager {
    private boolean enabled = false;
    private List<BackupData> dataList;
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
            dataList = backupList.stream()
                    .filter(s -> !Strings.isNullOrEmpty(s) && config.hasPath(s))
                    .map(config::getConfig)
                    .map(BackupManager::of)
                    .filter(data -> !Strings.isNullOrEmpty(data.source) && !Strings.isNullOrEmpty(data.sink))
                    .collect(Collectors.toList());
            dataModel.update(dataList);
        }
    }

    public void updateModel(JTable dataTable) {
        dataTable.setModel(dataModel);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static BackupData of(Config config) {
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

    private static final class BackupData {
        String source;
        String sink;
        int hour;
        int min;
        int backupMode;
        boolean backupEnabled;
        boolean zipEnabled;

    }

    private static final class BackupDataModel extends AbstractTableModel {
        private String[] columnName = {"源文件", "备份地址", "状态"};
        private Object[][] rowList = new Object[0][0];

        void update(List<BackupData> dataList) {
            Preconditions.checkNotNull(dataList, "data list == null");
            this.rowList = new Object[dataList.size()][3];
            for (int i = 0; i < dataList.size(); i++) {
                this.rowList[i][0] = dataList.get(i).source;
                this.rowList[i][1] = dataList.get(i).sink;
                this.rowList[i][2] = dataList.get(i).backupEnabled ? "开启" : "关闭";
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnName[column];
        }

        @Override
        public int getColumnCount() {
            return columnName.length;
        }

        @Override
        public int getRowCount() {
            return rowList.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rowList[rowIndex][columnIndex];
        }
    }
}
