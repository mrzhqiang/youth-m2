package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * @author mrzhqiang
 */
public final class HomeConfig {
  public String path;
  public String dbName;
  public String gameName;
  public String gameAddress;
  public boolean backupAction;
  public boolean wuxingAction;

  public final ServerConfig database = new ServerConfig();
  public final PublicServerConfig account = new PublicServerConfig();
  public final ServerConfig logger = new ServerConfig();
  public final IntervalServerConfig core = new IntervalServerConfig();
  public final GateConfig game = new GateConfig();
  public final GateConfig role = new GateConfig();
  public final GateConfig login = new GateConfig();
  public final ProgramConfig rank = new GateConfig();

  public void load(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    path = config.getString("path");
    dbName = config.getString("dbName");
    gameName = config.getString("gameName");
    gameAddress = config.getString("gameAddress");
    backupAction = config.getBoolean("backupAction");
    wuxingAction = config.getBoolean("wuxingAction");
    database.load(config.getConfig("database"));
    account.load(config.getConfig("account"));
    logger.load(config.getConfig("logger"));
    core.load(config.getConfig("core"));
    game.load(config.getConfig("game"));
    role.load(config.getConfig("role"));
    login.load(config.getConfig("login"));
    rank.load(config.getConfig("rank"));
  }
}
