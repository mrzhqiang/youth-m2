package youthm2.bootstrap.backup;

import com.typesafe.config.Config;

/**
 * BackupData
 *
 * @author qiang.zhang
 */
public class BackupData {
  public String source;
  public String destination;
  public int hour;
  public int min;
  public int backupMode;
  public boolean backupEnabled;
  public boolean zipEnabled;

  public static BackupData of(Config config) {
    BackupData data = new BackupData();
    data.source = config.getString("source");
    data.destination = config.getString("destination");
    data.hour = config.getInt("hour");
    data.min = config.getInt("min");
    data.backupMode = config.getInt("backupMode");
    data.backupEnabled = config.getBoolean("backupEnabled");
    data.zipEnabled = config.getBoolean("zipEnabled");
    return data;
  }
}
