package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * @author mrzhqiang
 */
public final class GameConfig {
  private static final String GAME_DIR = "game.dir";
  private static final String GAME_NAME = "game.name";
  private static final String GAME_DATABASE = "game.database";
  private static final String GAME_ADDRESS = "game.address";
  private static final String GAME_ACTION_BACKUP = "game.action.backup";
  private static final String GAME_ACTION_WUXING = "game.action.wuxing";

  private String dir;
  private String name;
  private String database;
  private String address;
  private boolean backupAction;
  private boolean wuxingAction;

  private ServerConfig databaseConfig = new ServerConfig();
  private ServerConfig accountConfig = new ServerConfig();
  private ServerConfig loggerConfig = new ServerConfig();
  private ServerConfig coreConfig = new ServerConfig();
  private GateConfig gameConfig = new GateConfig();
  private GateConfig roleConfig = new GateConfig();
  private GateConfig loginConfig = new GateConfig();
  private FormConfig rankConfig = new FormConfig();

  public void load(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    dir = config.getString(GAME_DIR);
    name = config.getString(GAME_NAME);
    database = config.getString(GAME_DATABASE);
    address = config.getString(GAME_ADDRESS);
    backupAction = config.getBoolean(GAME_ACTION_BACKUP);
    wuxingAction = config.getBoolean(GAME_ACTION_WUXING);
    databaseConfig.load(config.getConfig("database"));
    accountConfig.load(config.getConfig("account"));
    loggerConfig.load(config.getConfig("logger"));
    coreConfig.load(config.getConfig("core"));
    gameConfig.load(config.getConfig("game"));
    roleConfig.load(config.getConfig("role"));
    loginConfig.load(config.getConfig("login"));
    rankConfig.load(config.getConfig("rank"));
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public boolean isBackupAction() {
    return backupAction;
  }

  public void setBackupAction(boolean backupAction) {
    this.backupAction = backupAction;
  }

  public boolean isWuxingAction() {
    return wuxingAction;
  }

  public void setWuxingAction(boolean wuxingAction) {
    this.wuxingAction = wuxingAction;
  }
}
