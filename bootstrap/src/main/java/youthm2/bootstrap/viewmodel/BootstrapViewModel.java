package youthm2.bootstrap.viewmodel;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.LoggerFactory;
import rx.Subscriber;
import youthm2.bootstrap.model.BackupModel;
import youthm2.bootstrap.model.BootstrapModel;
import youthm2.bootstrap.model.config.ProgramConfig;
import youthm2.bootstrap.model.config.PublicServerConfig;
import youthm2.bootstrap.model.config.ServerConfig;
import youthm2.common.Monitor;
import youthm2.common.model.AlertModel;
import youthm2.common.model.NetworkModel;

/**
 * 引导程序的视图模型。
 * <p>
 * 所谓视图模型，就是将视图与模型进行双向绑定，以便任何一方改变时，另外一方都会同时更新。
 *
 * @author qiang.zhang
 */
public final class BootstrapViewModel {
  @FXML TabPane pageTabPane;
  /* 1. 控制面板 */
  @FXML Button databaseServerButton;
  @FXML Label databaseServerLabel;
  @FXML Button accountServerButton;
  @FXML Label accountServerLabel;
  @FXML Button loggerServerButton;
  @FXML Label loggerServerLabel;
  @FXML Button coreServerButton;
  @FXML Label coreServerLabel;
  @FXML Button gameGateButton;
  @FXML Label gameGateLabel;
  @FXML Button roleGateButton;
  @FXML Label roleGateLabel;
  @FXML Button loginGateButton;
  @FXML Label loginGateLabel;
  @FXML Button rankPlugButton;
  @FXML Label rankPlugLabel;
  @FXML ChoiceBox<String> startModeChoiceBox;
  @FXML Spinner<Integer> hoursSpinner;
  @FXML Spinner<Integer> minutesSpinner;
  @FXML TextArea consoleTextArea;
  @FXML Button startGameButton;
  /* 2. 参数配置 */
  @FXML TabPane configTabPane;
  @FXML TextField homePathTextField;
  @FXML TextField databaseNameTextField;
  @FXML TextField gameNameTextField;
  @FXML TextField gameAddressTextField;
  @FXML CheckBox combineActionCheckBox;
  @FXML Spinner<Integer> databaseXSpinner;
  @FXML Spinner<Integer> databaseYSpinner;
  @FXML Spinner<Integer> databasePortSpinner;
  @FXML Spinner<Integer> databaseServerPortSpinner;
  @FXML CheckBox databaseEnabledCheckBox;
  @FXML TextField databasePathTextField;

  private final ControlViewModel controlViewModel = new ControlViewModel();
  private final ConfigViewModel configViewModel = new ConfigViewModel();

  private final BootstrapModel bootstrapModel = new BootstrapModel();
  private final BackupModel backupModel = new BackupModel();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    prepareStart();
    bootstrapModel.loadConfig(config -> {
      Config bootstrap = config.getConfig("bootstrap");
      controlViewModel.update(bootstrap);
      configViewModel.update(bootstrap);
      controlViewModel.console.append("配置加载完毕.");
    });
    backupModel.loadConfig();

    controlViewModel.database.bind(databaseServerButton, databaseServerLabel);
    controlViewModel.account.bind(accountServerButton, accountServerLabel);
    controlViewModel.logger.bind(loggerServerButton, loggerServerLabel);
    controlViewModel.core.bind(coreServerButton, coreServerLabel);
    controlViewModel.game.bind(gameGateButton, gameGateLabel);
    controlViewModel.role.bind(roleGateButton, roleGateLabel);
    controlViewModel.login.bind(loginGateButton, loginGateLabel);
    controlViewModel.rank.bind(rankPlugButton, rankPlugLabel);
    controlViewModel.startMode.bind(startModeChoiceBox, hoursSpinner, minutesSpinner);
    controlViewModel.console.bind(consoleTextArea);
    controlViewModel.startGame.bind(startGameButton);

    configViewModel.bind(homePathTextField, databaseNameTextField, gameNameTextField,
        gameAddressTextField, combineActionCheckBox);
    //configViewModel.database.bind();

    startModeChoiceBox.selectionModelProperty().addListener((observable, oldValue, newValue) ->
        controlViewModel.startMode.select(newValue.getSelectedItem()));
    monitor.report("initialized");
  }

  private void prepareStart() {
    bootstrapModel.state = BootstrapModel.State.INITIALIZED;
    backupModel.status = 0;
    controlViewModel.startMode.normalMode();
    pageTabPane.getSelectionModel().select(0);
    configTabPane.getSelectionModel().select(0);
    controlViewModel.console.clean();
    // todo start list clear from apple m2
    controlViewModel.console.append("启动已就绪...");
  }

  @FXML void onDatabaseServerClicked() {
    controlViewModel.database.started();
  }

  @FXML void onAccountServerClicked() {
    controlViewModel.account.started();
  }

  @FXML void onLoggerServerClicked() {
    controlViewModel.logger.started();
  }

  @FXML void onCoreServerClicked() {
    controlViewModel.core.started();
  }

  @FXML void onGameGateClicked() {
    controlViewModel.game.started();
  }

  @FXML void onRoleGateClicked() {
    controlViewModel.role.started();
  }

  @FXML void onLoginGateClicked() {
    controlViewModel.login.started();
  }

  @FXML void onRankPlugClicked() {
    controlViewModel.rank.started();
  }

  @FXML void onStartGameClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText(bootstrapModel.state.getMessage());
    Optional<ButtonType> optional = alert.showAndWait()
        .filter(buttonType -> buttonType == ButtonType.OK);
    switch (bootstrapModel.state) {
      case INITIALIZED:
        optional.ifPresent(buttonType -> startServer());
        break;
      case STARTING:
        optional.ifPresent(buttonType -> cancelStartServer());
        break;
      case RUNNING:
        optional.ifPresent(buttonType -> stopServer());
        break;
      case STOPPING:
        optional.ifPresent(buttonType -> cancelStopServer());
        break;
    }
  }

  @FXML void onFoundHomePathClicked() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("请选择服务端目录");
    String home = bootstrapModel.config.home.getValue();
    if (Strings.isNullOrEmpty(home)) {
      home = System.getProperty("user.dir");
    }
    chooser.setInitialDirectory(new File(home));
    File file = chooser.showDialog(null);
    if (file != null) {
      if (file.exists() && file.isDirectory()) {
        homePathTextField.setText(file.getPath());
      }
    }
  }

  @FXML void onBasicDefaultClicked() {
    AlertModel.waitConfirm("恢复默认的基本配置？", "此操作将加载默认配置，请谨慎使用。")
        .filter(AlertModel.isOK())
        .ifPresent(buttonType -> bootstrapModel.loadBootstrapConfig(null));
  }

  @FXML void onBasicReloadClicked() {

  }

  @FXML void onBasicNextClicked() {
    configTabPane.getSelectionModel().selectNext();
  }

  @FXML void onFoundDatabaseFileClicked() {

  }

  @FXML void onFoundAccountFileClicked() {

  }

  @FXML void onFoundLoggerFileClicked() {

  }

  @FXML void onFoundCoreFileClicked() {

  }

  @FXML void onFoundGameFileClicked() {

  }

  @FXML void onFoundRoleFileClicked() {

  }

  @FXML void onFoundLoginFileClicked() {

  }

  @FXML void onFoundRankFileClicked() {

  }

  @FXML void onFoundExeFileClicked() {
    /*FileChooser chooser = new FileChooser();
    chooser.setTitle("请选择要启动的程序文件");
    String path = pathTextField.getText();
    if (Strings.isNullOrEmpty(path)) {
      path = bootstrapModel.config.home.getValue();
    }
    if (Strings.isNullOrEmpty(path)) {
      path = System.getProperty("user.dir");
    }
    File exeFile = new File(path);
    if (exeFile.isFile()) {
      chooser.setInitialDirectory(exeFile.getAbsoluteFile().getParentFile());
      chooser.setInitialFileName(exeFile.getName());
    } else {
      chooser.setInitialDirectory(exeFile);
    }
    // todo move the extension filter to common module
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("程序文件", "*.exe"));
    File file = chooser.showOpenDialog(null);
    if (file != null) {
      if (file.exists() && file.isFile()) {
        pathTextField.setText(file.getPath());
      }
    }*/
  }

  @FXML void onDefaultCurrentConfigClicked() {
    /*Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("是否恢复当前配置为默认值？");
    Toggle toggle = programSettingGroup.getSelectedToggle();
    if (toggle.equals(databaseRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadDatabaseConfig(null));
    } else if (toggle.equals(accountRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadAccountConfig(null));
    } else if (toggle.equals(loggerRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadLoggerConfig(null));
    } else if (toggle.equals(coreRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadCoreConfig(null));
    } else if (toggle.equals(gameRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadGameConfig(null));
    } else if (toggle.equals(roleRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadRoleConfig(null));
    } else if (toggle.equals(loginRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadLoginConfig(null));
    } else if (toggle.equals(rankRadioButton)) {
      alert.showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> bootstrapModel.loadRankConfig(null));
    }*/
  }

  @FXML void onReloadConfigClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("是否重新加载所有配置？");
    alert.showAndWait()
        .filter(buttonType -> buttonType == ButtonType.OK)
        .ifPresent(buttonType -> bootstrapModel.loadConfig(config -> {

        }));
  }

  @FXML void onSaveAllConfigClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("是否保存当前所有配置？");
    alert.showAndWait()
        .filter(buttonType -> buttonType == ButtonType.OK)
        .filter(buttonType -> checkConfig())
        .ifPresent(buttonType -> bootstrapModel.saveConfig());
  }

  private void bindProperty() {
  }

  private void cancelStopServer() {
    bootstrapModel.cancelStop();
  }

  private void stopServer() {
    bootstrapModel.stopServer();
  }

  private void cancelStartServer() {
    bootstrapModel.cancelStart();
    bootstrapModel.state = BootstrapModel.State.INITIALIZED;
  }

  private void startServer() {
    LocalDateTime targetTime = controlViewModel.startMode.computeStartDateTime();
    bootstrapModel.startServer(targetTime, new Subscriber<String>() {
      @Override public void onStart() {
        bootstrapModel.state = BootstrapModel.State.STARTING;
      }

      @Override public void onCompleted() {
        // interval 直到终结也不会回调
        LoggerFactory.getLogger("bootstrap").info("不可能回调完成逻辑吧！");
      }

      @Override public void onError(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("");
        controlViewModel.console.append(throwable.getMessage());
        bootstrapModel.state = BootstrapModel.State.INITIALIZED;
      }

      @Override public void onNext(String s) {
        controlViewModel.console.append(s);
        bootstrapModel.state = BootstrapModel.State.RUNNING;
      }
    });
  }

  private boolean checkConfig() {
    String home = bootstrapModel.config.getHome().trim();
    File file = new File(home);
    if (Strings.isNullOrEmpty(home) || !file.isDirectory() || !file.exists()) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的服务器目录，请检查");
      alert.show();
      homePathTextField.requestFocus();
      return false;
    }
    if (Strings.isNullOrEmpty(bootstrapModel.config.getDbName().trim())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的数据库名称，请检查");
      alert.show();
      databaseNameTextField.requestFocus();
      return false;
    }
    if (Strings.isNullOrEmpty(bootstrapModel.config.getGameName().trim())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏名称，请检查");
      alert.show();
      gameNameTextField.requestFocus();
      return false;
    }
    String address = bootstrapModel.config.getGameAddress();
    if (Strings.isNullOrEmpty(address) || !NetworkModel.isAddressV4(address)) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏 IP 地址，请检查");
      alert.show();
      gameAddressTextField.requestFocus();
      return false;
    }
    checkServerConfig(bootstrapModel.config.database);

    return true;
  }

  private boolean checkPublicServerConfig(PublicServerConfig config) {

    return false;
  }

  private boolean checkServerConfig(ServerConfig config) {
    if (checkProgramConfig(config)) {
      return true;
    }

    return false;
  }

  private boolean checkProgramConfig(ProgramConfig config) {
    if (!config.isEnabled()) {
      return true;
    }
    String path = config.getPath();
    File file = new File(path);
    return file.isFile() && file.exists();
  }

  private void handleProgramSettingChanged(Toggle oldValue, Toggle newValue) {
    unbindTargetConfig(oldValue);
    bindTargetConfig(newValue);
  }

  private void unbindTargetConfig(Toggle toggle) {

  }

  private void unbindProgramConfig(ProgramConfig config) {
    //enabledActionCheckBox.selectedProperty().unbindBidirectional(config.enabled);
    //xTextField.textProperty().unbindBidirectional(config.x);
    //yTextField.textProperty().unbindBidirectional(config.y);
    //portTextField.textProperty().unbindBidirectional(config.port);
    //pathTextField.textProperty().unbindBidirectional(config.path);
    //portTextField.setDisable(false);
  }

  private void unbindServerConfig(ServerConfig config) {
    //unbindProgramConfig(config);
    //serverTextField.textProperty().unbindBidirectional(config.serverPort);
    //serverTextField.setDisable(false);
  }

  private void unbindPublicServerConfig(PublicServerConfig config) {
    //unbindServerConfig(config);
    //publicServerTextField.textProperty().unbindBidirectional(config.publicPort);
    //publicServerTextField.setDisable(false);
  }

  private void bindTargetConfig(Toggle toggle) {
    /*if (toggle.equals(databaseRadioButton)) {
      bindServerConfig(bootstrapModel.config.database);
    } else if (toggle.equals(accountRadioButton)) {
      bindPublicServerConfig(bootstrapModel.config.account);
    } else if (toggle.equals(loggerRadioButton)) {
      bindServerConfig(bootstrapModel.config.logger);
    } else if (toggle.equals(coreRadioButton)) {
      bindServerConfig(bootstrapModel.config.core);
    } else if (toggle.equals(gameRadioButton)) {
      bindProgramConfig(bootstrapModel.config.game);
    } else if (toggle.equals(roleRadioButton)) {
      bindProgramConfig(bootstrapModel.config.role);
    } else if (toggle.equals(loginRadioButton)) {
      bindProgramConfig(bootstrapModel.config.login);
    } else if (toggle.equals(rankRadioButton)) {
      bindProgramConfig(bootstrapModel.config.rank);
    }*/
  }

  private void bindProgramConfig(ProgramConfig config) {
    //enabledActionCheckBox.selectedProperty().bindBidirectional(config.enabled);
    //xTextField.textProperty().bindBidirectional(config.x);
    //yTextField.textProperty().bindBidirectional(config.y);
    //portTextField.textProperty().bindBidirectional(config.port);
    //pathTextField.textProperty().bindBidirectional(config.path);
    //serverTextField.setDisable(true);
    //publicServerTextField.setDisable(true);
  }

  private void bindServerConfig(ServerConfig config) {
    //bindProgramConfig(config);
    //serverTextField.textProperty().bindBidirectional(config.serverPort);
    //serverTextField.setDisable(false);
    //publicServerTextField.setDisable(true);
  }

  private void bindPublicServerConfig(PublicServerConfig config) {
    //bindServerConfig(config);
    //publicServerTextField.textProperty().bindBidirectional(config.publicPort);
    //publicServerTextField.setDisable(false);
  }
}
