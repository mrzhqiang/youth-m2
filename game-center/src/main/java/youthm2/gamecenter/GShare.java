package youthm2.gamecenter;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公共逻辑。
 *
 * @author mrzhqiang
 */
final class GShare {
  private GShare() {
    throw new AssertionError("No instance.");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(GShare.class);

  private static final String CONFIG_FILE = ".\\Config.ini";
  private static final String BACKUP_CONFIG_FILE = ".\\BackupList.txt";

  private static final IConfig[] CONFIG_LIST = new IConfig[] {
      GameConfig.newInstant(),
      DatabaseConfig.newInstant(),
      AccountConfig.newInstant(),
      M2Config.newInstant(),
      LogConfig.newInstant(),
      RunConfig.newInstant(),
      SelConfig.newInstant(),
      LogConfig.newInstant(),
      PlugConfig.newInstant(),
  };
  private static final List<BackupConfig> BACKUP_CONFIG_LIST = Lists.newArrayList();

  private static Wini configWini;
  private static Wini backupWini;

  static void loadConfig() {
    initConfig();
    for (IConfig config : CONFIG_LIST) {
      config.readFrom(Util.getSection(configWini, config.sectionName()));
    }
  }

  static void saveConfig() {
    initConfig();
    for (IConfig config : CONFIG_LIST) {
      config.writeTo(Util.getSection(configWini, config.sectionName()));
    }
    try {
      configWini.store();
    } catch (IOException e) {
      String message = "Save game center config: " + CONFIG_FILE + " error.";
      LOGGER.error(message, e);
      throw new RuntimeException(message, e);
    }
  }

  static void loadBackupConfig() {
    initBackupConfig();
    synchronized (BACKUP_CONFIG_LIST) {
      backupWini.forEach((s, section) -> {
        BackupConfig config = BackupConfig.newInstant(Integer.parseInt(s));
        config.readFrom(section);
        if (config.isInvalid()) {
          return;
        }
        BACKUP_CONFIG_LIST.add(config);
      });
    }
  }

  static void saveBackupConfig() {
    initBackupConfig();
    synchronized (BACKUP_CONFIG_LIST) {
      BACKUP_CONFIG_LIST.forEach(backupConfig ->
          backupConfig.writeTo(Util.getSection(backupWini, backupConfig.sectionName())));
    }
    try {
      backupWini.store();
    } catch (IOException e) {
      String message = "Save back list config: " + BACKUP_CONFIG_FILE + " error.";
      LOGGER.error(message, e);
      throw new RuntimeException(message, e);
    }
  }

  static void start(GMain gMain) {
    // TODO
  }

  static void stop(GMain gMain) {

  }

  private static void initConfig() {
    if (configWini == null) {
      try {
        File input = new File(CONFIG_FILE);
        if (!input.exists() && input.createNewFile()) {
          LOGGER.info("Create game center config: " + input.getAbsolutePath());
        }
        configWini = new Wini(input);
      } catch (IOException e) {
        String message = "Load game center config: " + CONFIG_FILE + " error.";
        LOGGER.error(message, e);
        throw new RuntimeException(message, e);
      }
    }
  }

  private static void initBackupConfig() {
    if (backupWini == null) {
      try {
        File input = new File(BACKUP_CONFIG_FILE);
        if (!input.exists() && input.createNewFile()) {
          LOGGER.info("Create back list config: " + input.getAbsolutePath());
        }
        backupWini = new Wini(input);
      } catch (IOException e) {
        String message = "Load back list config: " + BACKUP_CONFIG_FILE + " error.";
        LOGGER.error(message, e);
        throw new RuntimeException(message, e);
      }
    }
  }
}
