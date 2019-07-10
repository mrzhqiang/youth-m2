package youthm2.bootstrap.viewmodel;

import com.google.common.base.Strings;
import helper.DateTimeHelper;
import java.io.File;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.LoggerFactory;
import rx.Subscriber;
import youthm2.bootstrap.model.BackupModel;
import youthm2.bootstrap.model.BootstrapModel;
import youthm2.bootstrap.model.config.ProgramConfig;
import youthm2.bootstrap.model.config.PublicServerConfig;
import youthm2.bootstrap.model.config.ServerConfig;
import youthm2.common.Network;
import youthm2.common.Monitor;

/**
 * 引导程序的视图模型。
 * <p>
 * 所谓视图模型，就是将视图与模型进行双向绑定，以便任何一方改变时，另外一方都会同时更新。
 *
 * @author qiang.zhang
 */
public final class BootstrapViewModel {
  private static final String FORMAT_CONSOLE = "[%s]: %s\r\n";
  private static final String REGEX_NUMBER_HUNDRED = "[0-9]+";
  private static final Integer MIN_TIME = 0;
  private static final Integer MAX_DELAY_HOURS = 99;
  private static final Integer MAX_DELAY_MINUTES = 59;
  private static final Integer MAX_TIMING_HOURS = 23;
  private static final Integer MAX_TIMING_MINUTES = 59;

  @FXML Accordion controlAccordion;
  @FXML TitledPane controlSwitchTitledPane;
  @FXML TitledPane controlTimeTitledPane;

  @FXML CheckBox databaseCheckBox;
  @FXML CheckBox accountCheckBox;
  @FXML CheckBox loggerCheckBox;
  @FXML CheckBox coreCheckBox;
  @FXML CheckBox gameCheckBox;
  @FXML CheckBox roleCheckBox;
  @FXML CheckBox loginCheckBox;
  @FXML CheckBox rankCheckBox;

  @FXML RadioButton normalModeRadioButton;
  @FXML RadioButton delayModeRadioButton;
  @FXML RadioButton timingModeRadioButton;
  @FXML ToggleGroup startModeGroup;
  @FXML TextField hoursTextField;
  @FXML TextField minutesTextField;

  @FXML TextArea consoleTextArea;
  @FXML Button startServerButton;

  @FXML Accordion parameterConfigAccordion;
  @FXML TitledPane basicConfigTitledPane;
  @FXML TextField homePathTextField;
  @FXML TextField databaseNameTextField;
  @FXML TextField gameNameTextField;
  @FXML TextField gameAddressTextField;
  @FXML CheckBox compoundActionCheckBox;

  @FXML RadioButton databaseRadioButton;
  @FXML RadioButton accountRadioButton;
  @FXML RadioButton loggerRadioButton;
  @FXML RadioButton coreRadioButton;
  @FXML RadioButton gameRadioButton;
  @FXML RadioButton roleRadioButton;
  @FXML RadioButton loginRadioButton;
  @FXML RadioButton rankRadioButton;
  @FXML ToggleGroup programSettingGroup;
  @FXML CheckBox enabledActionCheckBox;
  @FXML TextField xTextField;
  @FXML TextField yTextField;
  @FXML TextField portTextField;
  @FXML TextField serverTextField;
  @FXML TextField publicServerTextField;
  @FXML TextField pathTextField;

  private final BootstrapModel bootstrapModel = new BootstrapModel();
  private final BackupModel backupModel = new BackupModel();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    initLayout();
    monitor.record("init layout");
    initEvent();
    monitor.record("init event");
    bindProperty();
    monitor.record("bind property");
    loadConfig();
    monitor.record("load config");
    showConsole("启动已就绪！");
    monitor.report("bootstrap view model initialized");
  }

  @FXML void onStartServerClicked() {
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

  @FXML void onDefaultBasicConfigClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("是否恢复为默认的基本配置？");
    alert.showAndWait()
        .filter(buttonType -> buttonType == ButtonType.OK)
        .ifPresent(buttonType -> bootstrapModel.loadBootstrapConfig(null));
  }

  @FXML void onFoundGamePathClicked() {
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

  @FXML void onFoundExeFileClicked() {
    FileChooser chooser = new FileChooser();
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
    }
  }

  @FXML void onDefaultCurrentConfigClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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
    }
  }

  @FXML void onReloadConfigClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("是否重新加载所有配置？");
    alert.showAndWait()
        .filter(buttonType -> buttonType == ButtonType.OK)
        .ifPresent(buttonType -> bootstrapModel.loadConfig());
  }

  @FXML void onSaveAllConfigClicked() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText("是否保存当前所有配置？");
    alert.showAndWait()
        .filter(buttonType -> buttonType == ButtonType.OK)
        .filter(buttonType -> checkConfig())
        .ifPresent(buttonType -> bootstrapModel.saveConfig());
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
    startServerButton.setText(bootstrapModel.state.toString());
  }

  private void startServer() {
    LocalDateTime targetTime = computeStartDateTime();
    bootstrapModel.startServer(targetTime, new Subscriber<String>() {
      @Override public void onStart() {
        bootstrapModel.state = BootstrapModel.State.STARTING;
        startServerButton.setText(bootstrapModel.state.toString());
      }

      @Override public void onCompleted() {
        // interval 直到终结也不会回调
        LoggerFactory.getLogger("bootstrap").info("不可能回调完成逻辑吧！");
      }

      @Override public void onError(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("");
        showConsole(throwable.getMessage());
        bootstrapModel.state = BootstrapModel.State.INITIALIZED;
        startServerButton.setText(bootstrapModel.state.toString());
      }

      @Override public void onNext(String s) {
        showConsole(s);
        bootstrapModel.state = BootstrapModel.State.RUNNING;
        startServerButton.setText(bootstrapModel.state.toString());
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
    if (Strings.isNullOrEmpty(address) || !Network.isAddressV4(address)) {
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
    if (file.isFile() && file.exists()) {
      return true;
    }
    return false;
  }

  private void initLayout() {
    controlAccordion.setExpandedPane(controlSwitchTitledPane);
    //controlSwitchTitledPane.setExpanded(true);
    //controlTimeTitledPane.setExpanded(false);
    startModeGroup.selectToggle(normalModeRadioButton);
    hoursTextField.setText(MIN_TIME.toString());
    minutesTextField.setText(MIN_TIME.toString());
    hoursTextField.setDisable(true);
    minutesTextField.setDisable(true);
    consoleTextArea.clear();

    parameterConfigAccordion.setExpandedPane(basicConfigTitledPane);
    homePathTextField.clear();
    databaseNameTextField.clear();
    gameNameTextField.clear();
    gameAddressTextField.clear();
    programSettingGroup.selectToggle(databaseRadioButton);
  }

  private void initEvent() {
    startModeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
        handleStartModeRadioChanged(newValue));
    hoursTextField.textProperty().addListener((observable, oldValue, newValue) ->
        handleHoursTextChanged(newValue, oldValue));
    minutesTextField.textProperty().addListener((observable, oldValue, newValue) ->
        handleMinutesTextChanged(newValue, oldValue));
    programSettingGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
        handleProgramSettingChanged(oldValue, newValue));
  }

  private void bindProperty() {
    // bindBidirectional 是指双向绑定，意味着数据改动会影响组件状态，同时组件状态改变会更新到数据。
    // 需要注意的是：绑定从一开始就将目标对象的值设置给调用者；解绑则只是移除了监听器。
    databaseCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.database.enabled);
    accountCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.account.enabled);
    loggerCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.logger.enabled);
    coreCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.core.enabled);
    gameCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.game.enabled);
    roleCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.role.enabled);
    loginCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.login.enabled);
    rankCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.rank.enabled);

    homePathTextField.textProperty().bindBidirectional(bootstrapModel.config.home);
    databaseNameTextField.textProperty().bindBidirectional(bootstrapModel.config.dbName);
    gameNameTextField.textProperty().bindBidirectional(bootstrapModel.config.gameName);
    gameAddressTextField.textProperty().bindBidirectional(bootstrapModel.config.gameAddress);
    compoundActionCheckBox.selectedProperty()
        .bindBidirectional(bootstrapModel.config.compoundAction);
    bindTargetConfig(databaseRadioButton);
    // 其他组件不需要双向绑定，比如启动模式单选按钮组，点击切换又不影响配置。
    // 再比如控制台文本区域，只显示每次启动关闭时的信息，不需要保存到配置。
  }

  private void loadConfig() {
    bootstrapModel.loadConfig();
    backupModel.loadConfig();
  }

  private void showConsole(String message) {
    String timestamp = DateTimeHelper.format(Date.from(Instant.now()));
    String console = String.format(Locale.getDefault(), FORMAT_CONSOLE, timestamp, message);
    consoleTextArea.appendText(console);
  }

  private LocalDateTime computeStartDateTime() {
    LocalDateTime now = LocalDateTime.now();
    String hoursText = hoursTextField.getText();
    String minutesText = minutesTextField.getText();
    int hours = Integer.parseInt(hoursText);
    int minutes = Integer.parseInt(minutesText);
    if (delayModeRadioButton.isSelected()) {
      return now.plusHours(hours).plusMinutes(minutes);
    }
    if (timingModeRadioButton.isSelected()) {
      LocalDateTime timing = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hours, minutes));
      // 如果现在已经超过定时，那么给定时加个鸡腿（加一天）
      if (now.isAfter(timing)) {
        timing = timing.plusDays(1);
      }
      return timing;
    }
    // 正常模式，直接启动
    return now;
  }

  private void handleStartModeRadioChanged(Toggle newValue) {
    boolean isNormalMode = newValue.equals(normalModeRadioButton);
    hoursTextField.setDisable(isNormalMode);
    minutesTextField.setDisable(isNormalMode);
  }

  private void handleHoursTextChanged(String newValue, String oldValue) {
    if (Strings.isNullOrEmpty(newValue)) {
      return;
    }
    if (!Pattern.matches(REGEX_NUMBER_HUNDRED, newValue) || newValue.length() >= 3) {
      hoursTextField.setText(oldValue);
      return;
    }
    try {
      long value = Long.parseLong(newValue);
      if (delayModeRadioButton.isSelected() && value > MAX_DELAY_HOURS) {
        hoursTextField.setText(MAX_DELAY_HOURS.toString());
      }
      if (timingModeRadioButton.isSelected() && value > MAX_TIMING_HOURS) {
        hoursTextField.setText(MAX_TIMING_HOURS.toString());
      }
    } catch (Exception e) {
      hoursTextField.setText(oldValue);
    }
  }

  private void handleMinutesTextChanged(String newValue, String oldValue) {
    if (Strings.isNullOrEmpty(newValue)) {
      return;
    }
    if (!Pattern.matches(REGEX_NUMBER_HUNDRED, newValue) || newValue.length() >= 3) {
      minutesTextField.setText(oldValue);
      return;
    }
    try {
      long value = Long.parseLong(newValue);
      if (delayModeRadioButton.isSelected() && value > MAX_DELAY_MINUTES) {
        minutesTextField.setText(MAX_DELAY_MINUTES.toString());
      }
      if (timingModeRadioButton.isSelected() && value > MAX_TIMING_MINUTES) {
        minutesTextField.setText(MAX_TIMING_MINUTES.toString());
      }
    } catch (Exception e) {
      minutesTextField.setText(oldValue);
    }
  }

  private void handleProgramSettingChanged(Toggle oldValue, Toggle newValue) {
    unbindTargetConfig(oldValue);
    bindTargetConfig(newValue);
  }

  private void unbindTargetConfig(Toggle toggle) {
    if (toggle.equals(databaseRadioButton)) {
      unbindServerConfig(bootstrapModel.config.database);
    } else if (toggle.equals(accountRadioButton)) {
      unbindPublicServerConfig(bootstrapModel.config.account);
    } else if (toggle.equals(loggerRadioButton)) {
      unbindServerConfig(bootstrapModel.config.logger);
    } else if (toggle.equals(coreRadioButton)) {
      unbindServerConfig(bootstrapModel.config.core);
    } else if (toggle.equals(gameRadioButton)) {
      unbindProgramConfig(bootstrapModel.config.game);
    } else if (toggle.equals(roleRadioButton)) {
      unbindProgramConfig(bootstrapModel.config.role);
    } else if (toggle.equals(loginRadioButton)) {
      unbindProgramConfig(bootstrapModel.config.login);
    } else if (toggle.equals(rankRadioButton)) {
      unbindProgramConfig(bootstrapModel.config.rank);
    }
  }

  private void unbindProgramConfig(ProgramConfig config) {
    enabledActionCheckBox.selectedProperty().unbindBidirectional(config.enabled);
    xTextField.textProperty().unbindBidirectional(config.x);
    yTextField.textProperty().unbindBidirectional(config.y);
    portTextField.textProperty().unbindBidirectional(config.port);
    pathTextField.textProperty().unbindBidirectional(config.path);
    portTextField.setDisable(false);
  }

  private void unbindServerConfig(ServerConfig config) {
    unbindProgramConfig(config);
    serverTextField.textProperty().unbindBidirectional(config.serverPort);
    serverTextField.setDisable(false);
  }

  private void unbindPublicServerConfig(PublicServerConfig config) {
    unbindServerConfig(config);
    publicServerTextField.textProperty().unbindBidirectional(config.publicPort);
    publicServerTextField.setDisable(false);
  }

  private void bindTargetConfig(Toggle toggle) {
    if (toggle.equals(databaseRadioButton)) {
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
    }
  }

  private void bindProgramConfig(ProgramConfig config) {
    enabledActionCheckBox.selectedProperty().bindBidirectional(config.enabled);
    xTextField.textProperty().bindBidirectional(config.x);
    yTextField.textProperty().bindBidirectional(config.y);
    portTextField.textProperty().bindBidirectional(config.port);
    pathTextField.textProperty().bindBidirectional(config.path);
    serverTextField.setDisable(true);
    publicServerTextField.setDisable(true);
  }

  private void bindServerConfig(ServerConfig config) {
    bindProgramConfig(config);
    serverTextField.textProperty().bindBidirectional(config.serverPort);
    serverTextField.setDisable(false);
    publicServerTextField.setDisable(true);
  }

  private void bindPublicServerConfig(PublicServerConfig config) {
    bindServerConfig(config);
    publicServerTextField.textProperty().bindBidirectional(config.publicPort);
    publicServerTextField.setDisable(false);
  }
}
