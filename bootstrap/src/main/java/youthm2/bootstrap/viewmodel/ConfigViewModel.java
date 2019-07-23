package youthm2.bootstrap.viewmodel;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
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

  private final StringProperty homePath = new SimpleStringProperty();
  private final StringProperty databaseName = new SimpleStringProperty();
  private final StringProperty gameName = new SimpleStringProperty();
  private final StringProperty gameAddress = new SimpleStringProperty();
  private final BooleanProperty combineAction = new SimpleBooleanProperty();

  final ServerConfigViewModel database = new ServerConfigViewModel();
  final PublicServerConfigViewModel account = new PublicServerConfigViewModel();
  final ProgramConfigViewModel logger = new ProgramConfigViewModel();
  final ServerConfigViewModel core = new ServerConfigViewModel();
  final ProgramConfigViewModel game = new ProgramConfigViewModel();
  final ProgramConfigViewModel role = new ProgramConfigViewModel();
  final ProgramConfigViewModel login = new ProgramConfigViewModel();
  final ProgramConfigViewModel rank = new ProgramConfigViewModel();

  void update(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    updateBasic(config);
    database.update(config.getConfig("database"));
    account.update(config.getConfig("account"));
    logger.update(config.getConfig("logger"));
    core.update(config.getConfig("core"));
    game.update(config.getConfig("game"));
    role.update(config.getConfig("role"));
    login.update(config.getConfig("login"));
    rank.update(config.getConfig("rank"));
  }

  void updateBasic(Config config) {
    homePath.setValue(config.getString("home"));
    databaseName.setValue(config.getString("dbName"));
    gameName.setValue(config.getString("gameName"));
    gameAddress.setValue(config.getString("gameAddress"));
    combineAction.setValue(config.getBoolean("combineAction"));
  }

  void bind(TextField homePath, TextField dbName, TextField gameName, TextField gameAddress,
      CheckBox combineAction) {
    homePath.textProperty().bindBidirectional(this.homePath);
    dbName.textProperty().bindBidirectional(this.databaseName);
    gameName.textProperty().bindBidirectional(this.gameName);
    gameAddress.textProperty().bindBidirectional(this.gameAddress);
    combineAction.selectedProperty().bindBidirectional(this.combineAction);
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
    final ObjectProperty<SpinnerValueFactory<Integer>> port =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    void update(Config config) {
      Preconditions.checkNotNull(config, "config == null");
      x.getValue().setValue(config.getInt("x"));
      y.getValue().setValue(config.getInt("y"));
      enabled.setValue(config.getBoolean("enabled"));
      path.setValue(config.getString("path"));
      if (config.hasPath("port")) {
        port.getValue().setValue(config.getInt("port"));
      }
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        @Nullable Spinner<Integer> port) {
      x.valueFactoryProperty().bindBidirectional(this.x);
      y.valueFactoryProperty().bindBidirectional(this.y);
      enabled.selectedProperty().bindBidirectional(this.enabled);
      path.textProperty().bindBidirectional(this.path);
      if (port != null) {
        port.valueFactoryProperty().bindBidirectional(this.port);
      }
    }
  }

  /**
   * 服务配置视图模型。
   */
  static class ServerConfigViewModel extends ProgramConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> serverPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    @Override void update(Config config) {
      super.update(config);
      if (config.hasPath("serverPort")) {
        serverPort.getValue().setValue(config.getInt("serverPort"));
      }
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, Spinner<Integer> serverPort) {
      bind(x, y, enabled, path, port);
      serverPort.valueFactoryProperty().bindBidirectional(this.serverPort);
    }
  }

  /**
   * 公开服务配置视图模型。
   */
  static class PublicServerConfigViewModel extends ServerConfigViewModel {
    final ObjectProperty<SpinnerValueFactory<Integer>> publicPort =
        new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

    @Override void update(Config config) {
      super.update(config);
      if (config.hasPath("publicPort")) {
        publicPort.getValue().setValue(config.getInt("publicPort"));
      }
    }

    void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
        Spinner<Integer> port, Spinner<Integer> serverPort, Spinner<Integer> publicPort) {
      bind(x, y, enabled, path, port, serverPort);
      publicPort.valueFactoryProperty().bindBidirectional(this.publicPort);
    }
  }
}
