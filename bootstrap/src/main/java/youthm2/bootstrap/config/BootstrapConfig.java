package youthm2.bootstrap.config;

/**
 * 引导配置。
 *
 * @author qiang.zhang
 */
public final class BootstrapConfig {
  public String path;
  public String dbName;
  public String gameName;
  public String gameAddress;
  public boolean backupAction;
  public boolean wuxingAction;

  public final ServerConfig database = new ServerConfig();
  public final PublicServerConfig account = new PublicServerConfig();
  public final ServerConfig logger = new ServerConfig();
  public final ServerConfig core = new ServerConfig();
  public final ProgramConfig game = new ProgramConfig();
  public final ProgramConfig role = new ProgramConfig();
  public final ProgramConfig login = new ProgramConfig();
  public final ProgramConfig rank = new ProgramConfig();
}
