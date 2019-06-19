package youthm2.bootstrap;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import youthm2.bootstrap.config.*;

import java.io.File;

final class BootstrapConfig {
    String homePath;
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

    void load(File configFile) {
        Preconditions.checkNotNull(configFile, "config file == null");
        // 以 config file 为主，缺失的由默认配置 reference.conf 填补
        Config config = ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load());
        homePath = config.getString("homePath");
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
