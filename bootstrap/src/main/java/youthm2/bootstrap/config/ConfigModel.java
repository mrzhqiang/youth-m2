package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * 配置模型。
 *
 * @author mrzhqiang
 */
public final class ConfigModel {
  public final BootstrapConfig config = new BootstrapConfig();

  public void load(Config conf) {
    Preconditions.checkNotNull(conf, "config == null");
    Config bootstrap = conf.getConfig("bootstrap");
    Preconditions.checkNotNull(bootstrap, "bootstrap config == null");
    config.path = bootstrap.getString("path");
    config.dbName = bootstrap.getString("dbName");
    config.gameName = bootstrap.getString("gameName");
    config.gameAddress = bootstrap.getString("gameAddress");
    config.backupAction = bootstrap.getBoolean("backupAction");
    config.wuxingAction = bootstrap.getBoolean("wuxingAction");
    config.database.onLoad(bootstrap.getConfig("database"));
    config.account.onLoad(bootstrap.getConfig("account"));
    config.logger.onLoad(bootstrap.getConfig("logger"));
    config.core.onLoad(bootstrap.getConfig("core"));
    config.game.onLoad(bootstrap.getConfig("game"));
    config.role.onLoad(bootstrap.getConfig("role"));
    config.login.onLoad(bootstrap.getConfig("login"));
    config.rank.onLoad(bootstrap.getConfig("rank"));
  }
}
