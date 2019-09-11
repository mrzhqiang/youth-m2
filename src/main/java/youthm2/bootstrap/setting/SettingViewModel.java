package youthm2.bootstrap.setting;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import youthm2.bootstrap.config.BootstrapConfig;

/**
 * 参数配置视图模型。
 *
 * @author qiang.zhang
 */
public final class SettingViewModel {
  public final StringProperty homePath = new SimpleStringProperty();
  public final StringProperty databaseName = new SimpleStringProperty();
  public final StringProperty gameName = new SimpleStringProperty();
  public final StringProperty gameHost = new SimpleStringProperty();
  public final BooleanProperty backupAction = new SimpleBooleanProperty();
  public final BooleanProperty combineAction = new SimpleBooleanProperty();
  public final BooleanProperty wishAction = new SimpleBooleanProperty();

  public final ServerSettingViewModel database = new ServerSettingViewModel();
  public final MonServerSettingViewModel account = new MonServerSettingViewModel();
  public final ServerSettingViewModel logger = new ServerSettingViewModel();
  public final ServerSettingViewModel core = new ServerSettingViewModel();
  public final GateSettingViewModel game = new GateSettingViewModel();
  public final GateSettingViewModel role = new GateSettingViewModel();
  public final GateSettingViewModel login = new GateSettingViewModel();
  public final ProgramSettingViewModel rank = new ProgramSettingViewModel();

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

  public void update(BootstrapConfig config) {
    if (config != null) {
      homePath.setValue(config.homePath);
      databaseName.setValue(config.dbName);
      gameName.setValue(config.gameName);
      gameHost.setValue(config.gameHost);
      backupAction.setValue(config.backupAction);
      combineAction.setValue(config.combineAction);
      wishAction.setValue(config.wishAction);
      database.config.setValue(config.database);
      account.config.setValue(config.account);
      logger.config.setValue(config.logger);
      core.config.setValue(config.core);
      game.config.setValue(config.game);
      role.config.setValue(config.role);
      login.config.setValue(config.login);
      rank.config.setValue(config.rank);
    }
  }
}
