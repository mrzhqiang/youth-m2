package youthm2.bootstrap.model.backup;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 备份数据。
 *
 * @author mrzhqiang
 */
public final class BackupData {
  public final StringProperty source = new SimpleStringProperty("");
  public final StringProperty sink = new SimpleStringProperty("");
  public final IntegerProperty hour = new SimpleIntegerProperty(0);
  public final IntegerProperty min = new SimpleIntegerProperty(0);
  public final IntegerProperty backupMode = new SimpleIntegerProperty(0);
  public final BooleanProperty backupEnabled = new SimpleBooleanProperty(false);
  public final BooleanProperty zipEnabled = new SimpleBooleanProperty(false);
}
