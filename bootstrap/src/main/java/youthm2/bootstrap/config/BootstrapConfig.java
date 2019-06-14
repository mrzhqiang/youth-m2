package youthm2.bootstrap.config;

/**
 * BootstrapConfig
 *
 * @author qiang.zhang
 */
public class BootstrapConfig {
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

  public static class ProgramConfig {
    public int x;
    public int y;
    public boolean enabled;
    public String filename;
  }

  public static class ServerConfig extends ProgramConfig {
    public int port;
  }

  public static class IntervalServerConfig extends ServerConfig{
    public int serverPort;
  }

  public static class PublicServerConfig extends IntervalServerConfig {
    public int monPort;
  }

  public static class GateConfig extends ProgramConfig {
    public int port;
  }
}
