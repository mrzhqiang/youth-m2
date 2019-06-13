package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * @author mrzhqiang
 */
public final class HomeConfig {
  private static final String DIR = "dir";
  private static final String NAME = "name";
  private static final String DATABASE = "db";
  private static final String ADDRESS = "address";
  private static final String ACTION_BACKUP = "action.backup";
  private static final String ACTION_WUXING = "action.wuxing";

  private String dir;
  private String name;
  private String db;
  private String address;
  private boolean backupAction;
  private boolean wuxingAction;

  public final ServerConfig databaseServer = new ServerConfig();
  public final ServerConfig accountServer = new ServerConfig();
  public final ServerConfig loggerServer = new ServerConfig();
  public final ServerConfig coreServer = new ServerConfig();
  public final GateConfig gameGate = new GateConfig();
  public final GateConfig roleGate = new GateConfig();
  public final GateConfig loginGate = new GateConfig();
  public final FormConfig rankPlug = new FormConfig();

  public void load(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    if (!config.hasPath("home")) {
      return;
    }
    config = config.getConfig("home");
    dir = config.getString(DIR);
    name = config.getString(NAME);
    db = config.getString(DATABASE);
    address = config.getString(ADDRESS);
    backupAction = config.getBoolean(ACTION_BACKUP);
    wuxingAction = config.getBoolean(ACTION_WUXING);
    databaseServer.load(config.getConfig("database"));
    accountServer.load(config.getConfig("account"));
    loggerServer.load(config.getConfig("logger"));
    coreServer.load(config.getConfig("core"));
    gameGate.load(config.getConfig("game"));
    roleGate.load(config.getConfig("role"));
    loginGate.load(config.getConfig("login"));
    rankPlug.load(config.getConfig("rank"));
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

  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
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
