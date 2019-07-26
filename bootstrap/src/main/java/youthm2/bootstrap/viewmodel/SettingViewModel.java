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
final class SettingViewModel {
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

  final ObjectProperty<BootstrapConfig> config = new SimpleObjectProperty<>();

  SettingViewModel() {
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

    final ObjectProperty<ProgramConfig> config = new SimpleObjectProperty<>();

    ProgramConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
      });
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path) {
      x.valueFactoryProperty().bindBidirectional(this.x);
      y.valueFactoryProperty().bindBidirectional(this.y);
      enabled.selectedProperty().bindBidirectional(this.enabled);
      path.textProperty().bindBidirectional(this.path);
    }
  }

  /**
   * 网关配置视图模型。
   */
  static class GateConfigViewModel extends ProgramConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> port =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    final ObjectProperty<GateConfig> config = new SimpleObjectProperty<>();

    GateConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
        port.get().setValue(newValue.port);
      });
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port) {
      super.bind(x, y, enabled, path);
      port.valueFactoryProperty().bindBidirectional(this.port);
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

    final ObjectProperty<ServerConfig> config = new SimpleObjectProperty<>();

    ServerConfigViewModel() {
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

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
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
  static class MonServerConfigViewModel extends ServerConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> monPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    final ObjectProperty<MonServerConfig> config = new SimpleObjectProperty<>();

    MonServerConfigViewModel() {
      config.addListener((observable, oldValue, newValue) -> {
        x.get().setValue(newValue.x);
        y.get().setValue(newValue.y);
        enabled.setValue(newValue.enabled);
        path.setValue(newValue.path);
        port.get().setValue(newValue.port);
        serverPort.get().setValue(newValue.serverPort);
      });
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, Spinner<Integer> serverPort, Spinner<Integer> publicPort) {
      super.bind(x, y, enabled, path, port, serverPort);
      publicPort.valueFactoryProperty().bindBidirectional(this.monPort);
    }
  }
}
