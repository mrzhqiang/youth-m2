package youthm2.bootstrap.backup;

import com.google.common.base.Preconditions;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * BackupDataModel
 *
 * @author qiang.zhang
 */
public final class BackupDataModel extends AbstractTableModel {
  private String[] columnName = {"源文件", "备份地址", "状态"};
  private Object[][] rowList = new Object[0][0];
  private List<BackupData> dataList;

  public void update(List<BackupData> dataList) {
    Preconditions.checkNotNull(dataList, "data list == null");
    this.dataList = dataList;
    this.rowList = new Object[dataList.size()][3];
    for (int i = 0; i < dataList.size(); i++) {
      this.rowList[i][0] = dataList.get(i).source;
      this.rowList[i][1] = dataList.get(i).destination;
      this.rowList[i][2] = dataList.get(i).backupEnabled ? "开启" : "关闭";
    }
  }

  @Override public String getColumnName(int column) {
    return columnName[column];
  }

  @Override public int getColumnCount() {
    return columnName.length;
  }

  @Override public int getRowCount() {
    return rowList.length;
  }

  @Override public Object getValueAt(int rowIndex, int columnIndex) {
    return rowList[rowIndex][columnIndex];
  }
}
