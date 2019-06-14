package youthm2.bootstrap;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * BootstrapConfig
 *
 * @author qiang.zhang
 */
public final class BootstrapConfig {
  public String homePath;
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

  public void load(File configFile) {
    Preconditions.checkNotNull(configFile, "config file == null");
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

  public static class ProgramConfig {
    public int x;
    public int y;
    public boolean enabled;
    public String filename;

    public void load(Config config) {
      Preconditions.checkNotNull(config, "config == null");
      if (config.hasPath("x")) {
        x = config.getInt("x");
      }
      if (config.hasPath("y")) {
        y = config.getInt("y");
      }
      if (config.hasPath("enabled")) {
        enabled = config.getBoolean("enabled");
      }
      if (config.hasPath("filename")) {
        filename = config.getString("filename");
      }
    }
  }

  public static class ServerConfig extends ProgramConfig {
    public int port;

    @Override
    public void load(Config config) {
      super.load(config);
      if (config.hasPath("port")) {
        port = config.getInt("port");
      }
    }
  }

  public static class IntervalServerConfig extends ServerConfig{
    public int serverPort;

    @Override
    public void load(Config config) {
      super.load(config);
      if (config.hasPath("serverPort")) {
        serverPort = config.getInt("serverPort");
      }
    }
  }

  public static class PublicServerConfig extends IntervalServerConfig {
    public int monPort;

    @Override
    public void load(Config config) {
      super.load(config);
      if (config.hasPath("monPort")) {
        monPort = config.getInt("monPort");
      }
    }
  }

  public static class GateConfig extends ProgramConfig {
    public int port;

    @Override
    public void load(Config config) {
      super.load(config);
      if (config.hasPath("port")) {
        port = config.getInt("port");
      }
    }
  }
}
