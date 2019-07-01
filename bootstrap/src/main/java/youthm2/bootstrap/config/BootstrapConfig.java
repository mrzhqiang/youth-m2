package youthm2.bootstrap.config;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 引导配置。
 *
 * @author qiang.zhang
 */
public final class BootstrapConfig {
  public final StringProperty path = new SimpleStringProperty("");
  public final StringProperty dbName = new SimpleStringProperty("");
  public final StringProperty gameName = new SimpleStringProperty("");
  public final StringProperty gameAddress = new SimpleStringProperty("");
  public final BooleanProperty backupAction = new SimpleBooleanProperty(false);
  public final BooleanProperty wuxingAction = new SimpleBooleanProperty(false);

  public final ServerConfig database = new ServerConfig();
  public final PublicServerConfig account = new PublicServerConfig();
  public final ServerConfig logger = new ServerConfig();
  public final ServerConfig core = new ServerConfig();
  public final ProgramConfig game = new ProgramConfig();
  public final ProgramConfig role = new ProgramConfig();
  public final ProgramConfig login = new ProgramConfig();
  public final ProgramConfig rank = new ProgramConfig();
}
