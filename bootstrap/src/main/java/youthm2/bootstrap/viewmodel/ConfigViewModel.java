package youthm2.bootstrap.viewmodel;

import com.google.common.base.Preconditions;
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
final class ConfigViewModel {
  private static final IntegerSpinnerValueFactory LOCATION_VALUE_FACTORY =
      new IntegerSpinnerValueFactory(0, 9999, 0);
  private static final IntegerSpinnerValueFactory PORT_VALUE_FACTORY =
      new IntegerSpinnerValueFactory(0, 65535, 0);

  final StringProperty homePath = new SimpleStringProperty();
  final StringProperty databaseName = new SimpleStringProperty();
  final StringProperty gameName = new SimpleStringProperty();
  final StringProperty gameHost = new SimpleStringProperty();
  final BooleanProperty backupAction = new SimpleBooleanProperty();
  final BooleanProperty combineAction = new SimpleBooleanProperty();
  final BooleanProperty wishAction = new SimpleBooleanProperty();

  final ServerConfigViewModel database = new ServerConfigViewModel();
  final MonServerConfigViewModel account = new MonServerConfigViewModel();
  final ServerConfigViewModel logger = new ServerConfigViewModel();
  final ServerConfigViewModel core = new ServerConfigViewModel();
  final GateConfigViewModel game = new GateConfigViewModel();
  final GateConfigViewModel role = new GateConfigViewModel();
  final GateConfigViewModel login = new GateConfigViewModel();
  final ProgramConfigViewModel rank = new ProgramConfigViewModel();

  void update(BootstrapConfig config) {
    Preconditions.checkNotNull(config, "bootstrap config == null");
    homePath.setValue(config.homePath);
    databaseName.setValue(config.dbName);
    gameName.setValue(config.gameName);
    gameHost.setValue(config.gameHost);
    backupAction.setValue(config.backupAction);
    combineAction.setValue(config.combineAction);
    wishAction.setValue(config.wishAction);
  }

  void bind(TextField homePath, TextField dbName, TextField gameName, TextField gameAddress,
      CheckBox backupAction, CheckBox combineAction, CheckBox wishAction) {
    homePath.textProperty().bindBidirectional(this.homePath);
    dbName.textProperty().bindBidirectional(this.databaseName);
    gameName.textProperty().bindBidirectional(this.gameName);
    gameAddress.textProperty().bindBidirectional(this.gameHost);
    backupAction.selectedProperty().bindBidirectional(this.backupAction);
    combineAction.selectedProperty().bindBidirectional(this.combineAction);
    wishAction.selectedProperty().bindBidirectional(this.wishAction);
  }

  BootstrapConfig config() {
    BootstrapConfig config = new BootstrapConfig();
    config.homePath = homePath.get();
    config.dbName = databaseName.get();
    config.gameName = gameName.get();
    config.gameHost = gameHost.get();
    config.backupAction = backupAction.get();
    config.combineAction = combineAction.get();
    config.wishAction = wishAction.get();
    config.database = database.get();
    config.account = account.get();
    config.logger = logger.get();
    config.core = core.get();
    config.game = game.get();
    config.role = role.get();
    config.login = login.get();
    config.rank = rank.get();
    return config;
  }

  /**
   * 程序配置视图模型。
   *
   * @author qiang.zhang
   */
  static class ProgramConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> x =
        new SimpleObjectProperty<>(LOCATION_VALUE_FACTORY);
    final ObjectProperty<SpinnerValueFactory<Integer>> y =
        new SimpleObjectProperty<>(LOCATION_VALUE_FACTORY);
    final BooleanProperty enabled = new SimpleBooleanProperty();
    final StringProperty path = new SimpleStringProperty();

    void update(ProgramConfig config) {
      Preconditions.checkNotNull(config, "config == null");
      x.getValue().setValue(config.x);
      y.getValue().setValue(config.y);
      enabled.setValue(config.enabled);
      path.setValue(config.path);
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path) {
      x.valueFactoryProperty().bindBidirectional(this.x);
      y.valueFactoryProperty().bindBidirectional(this.y);
      enabled.selectedProperty().bindBidirectional(this.enabled);
      path.textProperty().bindBidirectional(this.path);
    }

    ProgramConfig get() {
      ProgramConfig config = new ProgramConfig();
      config.x = x.get().getValue();
      config.y = y.get().getValue();
      config.enabled = enabled.get();
      config.path = path.get();
      return config;
    }
  }

  /**
   * 网关配置视图模型。
   *
   * @author qiang.zhang
   */
  static class GateConfigViewModel extends ProgramConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> port =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    void update(GateConfig config) {
      super.update(config);
      port.getValue().setValue(config.port);
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port) {
      super.bind(x, y, enabled, path);
      port.valueFactoryProperty().bindBidirectional(this.port);
    }

    GateConfig get() {
      GateConfig config = new GateConfig();
      config.x = x.get().getValue();
      config.y = y.get().getValue();
      config.enabled = enabled.get();
      config.path = path.get();
      config.port = port.get().getValue();
      return config;
    }
  }

  /**
   * 服务配置视图模型。
   */
  static class ServerConfigViewModel extends ProgramConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> port =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);
    final ObjectProperty<SpinnerValueFactory<Integer>> serverPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    void update(ServerConfig config) {
      super.update(config);
      port.getValue().setValue(config.port);
      serverPort.getValue().setValue(config.serverPort);
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, @Nullable Spinner<Integer> serverPort) {
      super.bind(x, y, enabled, path);
      port.valueFactoryProperty().bindBidirectional(this.port);
      if (serverPort != null) {
        serverPort.valueFactoryProperty().bindBidirectional(this.serverPort);
      }
    }

    ServerConfig get() {
      ServerConfig config = new ServerConfig();
      config.x = x.get().getValue();
      config.y = y.get().getValue();
      config.enabled = enabled.get();
      config.path = path.get();
      config.port = port.get().getValue();
      config.serverPort = serverPort.get().getValue();
      return config;
    }
  }

  /**
   * 通用服务配置视图模型。
   */
  static class MonServerConfigViewModel extends ServerConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> monPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    void update(MonServerConfig config) {
      super.update(config);
      monPort.getValue().setValue(config.monPort);
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, Spinner<Integer> serverPort, Spinner<Integer> publicPort) {
      super.bind(x, y, enabled, path, port, serverPort);
      publicPort.valueFactoryProperty().bindBidirectional(this.monPort);
    }

    MonServerConfig get() {
      MonServerConfig config = new MonServerConfig();
      config.x = x.get().getValue();
      config.y = y.get().getValue();
      config.enabled = enabled.get();
      config.path = path.get();
      config.port = port.get().getValue();
      config.serverPort = serverPort.get().getValue();
      config.monPort = monPort.get().getValue();
      return config;
    }
  }
}
