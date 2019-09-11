package youthm2.bootstrap.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javax.annotation.Nullable;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.bootstrap.model.config.GateConfig;
import youthm2.bootstrap.model.config.MonServerConfig;
import youthm2.bootstrap.model.config.ProgramConfig;
import youthm2.bootstrap.model.config.ServerConfig;

import static javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

/**
 * 参数配置视图模型。
 *
 * @author qiang.zhang
 */
public final class SettingViewModel {
  private static final IntegerSpinnerValueFactory LOCATION_VALUE_FACTORY =
      new IntegerSpinnerValueFactory(0, 9999, 0);
  private static final IntegerSpinnerValueFactory PORT_VALUE_FACTORY =
      new IntegerSpinnerValueFactory(0, 65535, 0);

  public final StringProperty homePath = new SimpleStringProperty();
  public final StringProperty databaseName = new SimpleStringProperty();
  public final StringProperty gameName = new SimpleStringProperty();
  public final StringProperty gameHost = new SimpleStringProperty();
  public final BooleanProperty backupAction = new SimpleBooleanProperty();
  public final BooleanProperty combineAction = new SimpleBooleanProperty();
  public final BooleanProperty wishAction = new SimpleBooleanProperty();

  public final ServerConfigViewModel database = new ServerConfigViewModel();
  public final MonServerConfigViewModel account = new MonServerConfigViewModel();
  public final ServerConfigViewModel logger = new ServerConfigViewModel();
  public final ServerConfigViewModel core = new ServerConfigViewModel();
  public final GateConfigViewModel game = new GateConfigViewModel();
  public final GateConfigViewModel role = new GateConfigViewModel();
  public final GateConfigViewModel login = new GateConfigViewModel();
  public final ProgramConfigViewModel rank = new ProgramConfigViewModel();

  public final ObjectProperty<BootstrapConfig> config = new SimpleObjectProperty<>();

  public SettingViewModel() {
    // 单方向的更新，用于加载 config 时的逻辑实现，简化外部调用
    config.addListener((observable, oldValue, newValue) -> {
      homePath.setValue(newValue.homePath);
      databaseName.setValue(newValue.dbName);
      gameName.setValue(newValue.gameName);
      gameHost.setValue(newValue.gameHost);
      backupAction.setValue(newValue.backupAction);
      combineAction.setValue(newValue.combineAction);
      wishAction.setValue(newValue.wishAction);
      database.config.setValue(newValue.database);
      account.config.setValue(newValue.account);
      logger.config.setValue(newValue.logger);
      core.config.setValue(newValue.core);
      game.config.setValue(newValue.game);
      role.config.setValue(newValue.role);
      login.config.setValue(newValue.login);
      rank.config.setValue(newValue.rank);
    });
  }

  public void bind(TextField homePath, TextField dbName, TextField gameName, TextField gameAddress,
      CheckBox backupAction, CheckBox combineAction, CheckBox wishAction) {
    homePath.textProperty().bindBidirectional(this.homePath);
    dbName.textProperty().bindBidirectional(this.databaseName);
    gameName.textProperty().bindBidirectional(this.gameName);
    gameAddress.textProperty().bindBidirectional(this.gameHost);
    backupAction.selectedProperty().bindBidirectional(this.backupAction);
    combineAction.selectedProperty().bindBidirectional(this.combineAction);
    wishAction.selectedProperty().bindBidirectional(this.wishAction);
  }

  /**
   * 程序配置视图模型。
   *
   * @author qiang.zhang
   */
  public static class ProgramConfigViewModel {
    public final ObjectProperty<SpinnerValueFactory<Integer>> x =
        new SimpleObjectProperty<>(LOCATION_VALUE_FACTORY);
    public final ObjectProperty<SpinnerValueFactory<Integer>> y =
        new SimpleObjectProperty<>(LOCATION_VALUE_FACTORY);
    public final BooleanProperty enabled = new SimpleBooleanProperty();
    public final StringProperty path = new SimpleStringProperty();

    public final ObjectProperty<ProgramConfig> config = new SimpleObjectProperty<>();

    public ProgramConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
      });
    }

    public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path) {
      x.valueFactoryProperty().bindBidirectional(this.x);
      y.valueFactoryProperty().bindBidirectional(this.y);
      enabled.selectedProperty().bindBidirectional(this.enabled);
      path.textProperty().bindBidirectional(this.path);
    }
  }

  /**
   * 网关配置视图模型。
   */
  public static class GateConfigViewModel extends ProgramConfigViewModel {
    public final ObjectProperty<SpinnerValueFactory<Integer>> port =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    public final ObjectProperty<GateConfig> config = new SimpleObjectProperty<>();

    public GateConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
        port.get().setValue(newValue.port);
      });
    }

    public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port) {
      super.bind(x, y, enabled, path);
      port.valueFactoryProperty().bindBidirectional(this.port);
    }
  }

  /**
   * 服务配置视图模型。
   */
  public static class ServerConfigViewModel extends ProgramConfigViewModel {
    public final ObjectProperty<SpinnerValueFactory<Integer>> port =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);
    public final ObjectProperty<SpinnerValueFactory<Integer>> serverPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    public final ObjectProperty<ServerConfig> config = new SimpleObjectProperty<>();

    public ServerConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
        port.get().setValue(newValue.port);
        if (newValue.serverPort != null) {
          serverPort.get().setValue(newValue.serverPort);
        }
      });
    }

    public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, @Nullable Spinner<Integer> serverPort) {
      super.bind(x, y, enabled, path);
      port.valueFactoryProperty().bindBidirectional(this.port);
      if (serverPort != null) {
        serverPort.valueFactoryProperty().bindBidirectional(this.serverPort);
      }
    }
  }

  /**
   * 通用服务配置视图模型。
   */
  public static class MonServerConfigViewModel extends ServerConfigViewModel {
    public final ObjectProperty<SpinnerValueFactory<Integer>> monPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    public final ObjectProperty<MonServerConfig> config = new SimpleObjectProperty<>();

    public MonServerConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
        port.get().setValue(newValue.port);
        serverPort.get().setValue(newValue.serverPort);
      });
    }

    public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, Spinner<Integer> serverPort, Spinner<Integer> publicPort) {
      super.bind(x, y, enabled, path, port, serverPort);
      publicPort.valueFactoryProperty().bindBidirectional(this.monPort);
    }
  }
}
