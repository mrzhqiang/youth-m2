package youthm2.bootstrap;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import youthm2.bootstrap.config.GateConfig;
import youthm2.bootstrap.config.IntervalServerConfig;
import youthm2.bootstrap.config.ProgramConfig;
import youthm2.bootstrap.config.PublicServerConfig;
import youthm2.bootstrap.config.ServerConfig;

/**
 * @author mrzhqiang
 */
final class HomeConfig {
  String path;
  String dbName;
  String gameName;
  String gameAddress;
  boolean backupAction;
  boolean wuxingAction;

  final ServerConfig database = new ServerConfig();
  final PublicServerConfig account = new PublicServerConfig();
  final ServerConfig logger = new ServerConfig();
  final IntervalServerConfig core = new IntervalServerConfig();
  final GateConfig game = new GateConfig();
  final GateConfig role = new GateConfig();
  final GateConfig login = new GateConfig();
  final ProgramConfig rank = new GateConfig();

  void load(Config config) {
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
