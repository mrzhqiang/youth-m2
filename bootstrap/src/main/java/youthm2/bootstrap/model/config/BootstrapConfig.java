package youthm2.bootstrap.model.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * BootstrapConfig
 *
 * @author qiang.zhang
 */
public final class BootstrapConfig {
  private static final String HOME_PATH = "homePath";
  private static final String DB_NAME = "dbName";
  private static final String GAME_NAME = "gameName";
  private static final String GAME_HOST = "gameHost";
  private static final String BACKUP_ACTION = "backupAction";
  private static final String COMBINE_ACTION = "combineAction";
  private static final String WISH_ACTION = "wishAction";

  private static final String CONFIG_DATABASE = "database";
  private static final String CONFIG_ACCOUNT = "account";
  private static final String CONFIG_LOGGER = "logger";
  private static final String CONFIG_CORE = "core";
  private static final String CONFIG_GAME = "game";
  private static final String CONFIG_ROLE = "role";
  private static final String CONFIG_LOGIN = "login";
  private static final String CONFIG_RANK = "rank";

  public String homePath;
  public String dbName;
  public String gameName;
  public String gameHost;
  public boolean backupAction;
  public boolean combineAction;
  public boolean wishAction;

  public ServerConfig database;
  public MonServerConfig account;
  public ServerConfig logger;
  public ServerConfig core;
  public GateConfig game;
  public GateConfig role;
  public GateConfig login;
  public ProgramConfig rank;

  public static BootstrapConfig of(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    BootstrapConfig bootstrap = new BootstrapConfig();
    bootstrap.homePath = config.getString(HOME_PATH);
    bootstrap.dbName = config.getString(DB_NAME);
    bootstrap.gameName = config.getString(GAME_NAME);
    bootstrap.gameHost = config.getString(GAME_HOST);
    bootstrap.backupAction = config.getBoolean(BACKUP_ACTION);
    bootstrap.combineAction = config.getBoolean(COMBINE_ACTION);
    bootstrap.wishAction = config.getBoolean(WISH_ACTION);
    bootstrap.database = ServerConfig.of(config.getConfig(CONFIG_DATABASE));
    bootstrap.account = MonServerConfig.of(config.getConfig(CONFIG_ACCOUNT));
    bootstrap.logger = ServerConfig.of(config.getConfig(CONFIG_LOGGER));
    bootstrap.core = ServerConfig.of(config.getConfig(CONFIG_CORE));
    bootstrap.game = GateConfig.of(config.getConfig(CONFIG_GAME));
    bootstrap.role = GateConfig.of(config.getConfig(CONFIG_ROLE));
    bootstrap.login = GateConfig.of(config.getConfig(CONFIG_LOGIN));
    bootstrap.rank = ProgramConfig.of(config.getConfig(CONFIG_RANK));
    return bootstrap;
  }
}
