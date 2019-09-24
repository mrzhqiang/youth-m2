package youthm2.bootstrap;

import com.google.common.base.Strings;
import com.typesafe.config.ConfigFactory;
import helper.DateTimeHelper;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import youthm2.bootstrap.config.BootstrapConfig;
import youthm2.bootstrap.config.Configs;
import youthm2.common.Monitor;
import youthm2.common.dialog.AlertDialog;
import youthm2.common.dialog.ChooserDialog;
import youthm2.common.dialog.ThrowableDialog;
import youthm2.database.DatabaseServer;

/**
 * 引导程序控制器。
 *
 * @author mrzhqiang
 */
public final class BootstrapController {
  private static final String MODE_NORMAL = "正常启动";
  private static final String MODE_DELAY = "延迟启动";
  private static final String MODE_TIMING = "定时启动";
  private static final ObservableList<String> START_MODE_ITEMS =
      FXCollections.observableArrayList(MODE_NORMAL, MODE_DELAY, MODE_TIMING);
  private static final SpinnerValueFactory.IntegerSpinnerValueFactory HOURS_VALUE_FACTORY =
      new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
  private static final SpinnerValueFactory.IntegerSpinnerValueFactory MINUTES_VALUE_FACTORY =
      new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
  private static final String FORMAT_CONSOLE = "[%s]: %s\r\n";

  /* root layout */
  @FXML TabPane pageTabPane;
  @FXML Tab controlTab;
  /* control layout */
  @FXML CheckBox databaseCheckBox;
  @FXML Label databaseServerLabel;
  @FXML CheckBox accountCheckBox;
  @FXML Label accountServerLabel;
  @FXML CheckBox loggerCheckBox;
  @FXML Label loggerServerLabel;
  @FXML CheckBox coreCheckBox;
  @FXML Label coreServerLabel;
  @FXML CheckBox gameCheckBox;
  @FXML Label gameGateLabel;
  @FXML CheckBox roleCheckBox;
  @FXML Label roleGateLabel;
  @FXML CheckBox loginCheckBox;
  @FXML Label loginGateLabel;
  @FXML CheckBox rankCheckBox;
  @FXML Label rankPlugLabel;
  @FXML ChoiceBox<String> startModeChoiceBox;
  @FXML Spinner<Integer> hoursSpinner;
  @FXML Spinner<Integer> minutesSpinner;
  @FXML TextArea consoleTextArea;
  @FXML Button startGameButton;
  /* setting layout */
  @FXML TabPane configTabPane;
  @FXML Tab baseConfigTab;
  @FXML TextField homePathTextField;
  @FXML TextField databaseNameTextField;
  @FXML TextField gameNameTextField;
  @FXML TextField gameAddressTextField;
  @FXML CheckBox backupActionCheckBox;
  @FXML CheckBox combineActionCheckBox;
  @FXML CheckBox wishActionCheckBox;
  @FXML Spinner<Integer> databaseXSpinner;
  @FXML Spinner<Integer> databaseYSpinner;
  @FXML Spinner<Integer> databasePortSpinner;
  @FXML Spinner<Integer> databaseServerPortSpinner;
  @FXML CheckBox databaseEnabledCheckBox;
  @FXML TextField databasePathTextField;
  @FXML Spinner<Integer> accountXSpinner;
  @FXML Spinner<Integer> accountYSpinner;
  @FXML Spinner<Integer> accountPortSpinner;
  @FXML Spinner<Integer> accountServerPortSpinner;
  @FXML Spinner<Integer> accountPublicPortSpinner;
  @FXML CheckBox accountEnabledCheckBox;
  @FXML TextField accountPathTextField;
  @FXML Spinner<Integer> loggerXSpinner;
  @FXML Spinner<Integer> loggerYSpinner;
  @FXML Spinner<Integer> loggerPortSpinner;
  @FXML CheckBox loggerEnabledCheckBox;
  @FXML TextField loggerPathTextField;
  @FXML Spinner<Integer> coreXSpinner;
  @FXML Spinner<Integer> coreYSpinner;
  @FXML Spinner<Integer> corePortSpinner;
  @FXML Spinner<Integer> coreServerPortSpinner;
  @FXML CheckBox coreEnabledCheckBox;
  @FXML TextField corePathTextField;
  @FXML Spinner<Integer> gameXSpinner;
  @FXML Spinner<Integer> gameYSpinner;
  @FXML Spinner<Integer> gamePortSpinner;
  @FXML CheckBox gameEnabledCheckBox;
  @FXML TextField gamePathTextField;
  @FXML Spinner<Integer> roleXSpinner;
  @FXML Spinner<Integer> roleYSpinner;
  @FXML Spinner<Integer> rolePortSpinner;
  @FXML CheckBox roleEnabledCheckBox;
  @FXML TextField rolePathTextField;
  @FXML Spinner<Integer> loginXSpinner;
  @FXML Spinner<Integer> loginYSpinner;
  @FXML Spinner<Integer> loginPortSpinner;
  @FXML CheckBox loginEnabledCheckBox;
  @FXML TextField loginPathTextField;
  @FXML Spinner<Integer> rankXSpinner;
  @FXML Spinner<Integer> rankYSpinner;
  @FXML CheckBox rankEnabledCheckBox;
  @FXML TextField rankPathTextField;

  // 0 -- default/stopped; 1 -- starting; 2 -- running; 3 -- stopping; 4 -- failed
  private int startStatus;
  // 0 -- disabled; 1 -- enabled
  private int backupStatus;

  private final ObjectProperty<BootstrapConfig> config =
      new SimpleObjectProperty<>(BootstrapConfig.of(ConfigFactory.load()));

  private final DatabaseServer databaseServer = new DatabaseServer();
  private final CompositeDisposable disposable = new CompositeDisposable();

  /** javafx.fxml.FXMLLoader.loadImpl(FXMLLoader.java:2566) */
  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    monitor.record("init begin");
    initEvent();
    monitor.record("init event");
    prepareStart();
    monitor.record("prepare start");
    loadConfig();
    monitor.record("load config");
    updateLayout();
    monitor.record("update layout");
    loadBackupList();
    monitor.record("load backup config");
    updateBackupLayout();
    monitor.record("update backup layout");
    monitor.report("init end");
  }

  void onDestroy() {
    disposable.dispose();
  }

  @FXML void onDatabaseServerClicked() {
    if (config.get().database.enabled) {
      AlertDialog.waitConfirm("是否启动数据库服务")
          .ifPresent(buttonType -> {
            // todo 需要一个时钟间隔线程，检查启动状态、运行状态、停止状态的情况。
            // 启动时，检查程序是否都启动
            // 运行时，检查程序是否都正常
            // 停止时，检查程序是否都停止
            databaseServer.start(new Stage());
          });
    }
  }

  @FXML void onStartGameClicked() {
    // todo 启动时钟间隔线程，设置开始启动所有程序
  }

  @FXML void onFoundHomePathClicked() {
    ChooserDialog.directory("请选择服务端目录", new File(getHomePath()))
        .ifPresent(file -> homePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundDatabaseFileClicked() {
    ChooserDialog.file("请选择要启动的数据库服务", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> databasePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundAccountFileClicked() {
    ChooserDialog.file("请选择要启动的账号服务", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> accountPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundLoggerFileClicked() {
    ChooserDialog.file("请选择要启动的日志服务", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> loggerPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundCoreFileClicked() {
    ChooserDialog.file("请选择要启动的核心服务", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> corePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundGameFileClicked() {
    ChooserDialog.file("请选择要启动的游戏网关", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> gamePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundRoleFileClicked() {
    ChooserDialog.file("请选择要启动的角色网关", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> rolePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundLoginFileClicked() {
    ChooserDialog.file("请选择要启动的登陆网关", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> loginPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundRankFileClicked() {
    ChooserDialog.file("请选择要启动的排行榜插件", new File(getHomePath()), ChooserDialog.exeFilter())
        .ifPresent(file -> rankPathTextField.setText(file.getPath()));
  }

  @FXML void onConfigDefaultClicked() {
    SingleSelectionModel<Tab> selectionModel = configTabPane.getSelectionModel();
    switch (selectionModel.getSelectedIndex()) {
      default:
      case 0:
        //AlertModel.waitConfirm("恢复默认基本配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(settingViewModel::update));
        break;
      case 1:
        //AlertModel.waitConfirm("恢复默认数据库配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.database.update(config.database)));
        break;
      case 2:
        //AlertModel.waitConfirm("恢复默认账号配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.account.update(config.account)));
        break;
      case 3:
        //AlertModel.waitConfirm("恢复默认日志配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.logger.update(config.logger)));
        break;
      case 4:
        //AlertModel.waitConfirm("恢复默认核心配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.core.update(config.core)));
        break;
      case 5:
        //AlertModel.waitConfirm("恢复默认游戏配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.game.update(config.game)));
        break;
      case 6:
        //AlertModel.waitConfirm("恢复默认角色配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.role.update(config.role)));
        break;
      case 7:
        //AlertModel.waitConfirm("恢复默认登陆配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.login.update(config.login)));
        break;
      case 8:
        //AlertModel.waitConfirm("恢复默认排行榜配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadDefaultConfig(
        //        config -> settingViewModel.rank.update(config.rank)));
        break;
    }
  }

  @FXML void onConfigReloadClicked() {
    SingleSelectionModel<Tab> selectionModel = configTabPane.getSelectionModel();
    switch (selectionModel.getSelectedIndex()) {
      default:
      case 0:
        //AlertModel.waitConfirm("重新加载基本配置？", "此操作将恢复当前配置为最近一次数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(settingViewModel::update));
        break;
      case 1:
        //AlertModel.waitConfirm("重新加载数据库配置？", "此操作将恢复当前配置为最近一次数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.database.update(config.database)));
        break;
      case 2:
        //AlertModel.waitConfirm("重新加载账号配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.account.update(config.account)));
        break;
      case 3:
        //AlertModel.waitConfirm("重新加载日志配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.logger.update(config.logger)));
        break;
      case 4:
        //AlertModel.waitConfirm("重新加载核心配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.core.update(config.core)));
        break;
      case 5:
        //AlertModel.waitConfirm("重新加载游戏配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.game.update(config.game)));
        break;
      case 6:
        //AlertModel.waitConfirm("重新加载角色配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.role.update(config.role)));
        break;
      case 7:
        //AlertModel.waitConfirm("重新加载登陆配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.login.update(config.login)));
        break;
      case 8:
        //AlertModel.waitConfirm("重新加载排行榜配置？", "此操作将恢复当前配置为内置默认数值。")
        //    .filter(AlertModel.isOK())
        //    .ifPresent(type -> bootstrapModel.loadConfig(
        //        config -> settingViewModel.rank.update(config.rank)));
        break;
    }
  }

  @FXML void onConfigPreviousClicked() {
    // 切换参数配置菜单到上一页时，不做检测；等保存时再全部检测一下。
    configTabPane.getSelectionModel().selectPrevious();
  }

  @FXML void onConfigNextClicked() {
    // 切换参数配置菜单到下一页时，不做检测；等保存时再全部检测一下。
    configTabPane.getSelectionModel().selectNext();
  }

  @FXML void onConfigSaveClicked() {
    //AlertModel.waitConfirm("保存当前所有配置？", "此操作将重新生成配置文件。")
    //    .filter(AlertModel.isOK())
    //    .map(type -> checkConfig())
    //    .ifPresent(type -> bootstrapModel.saveConfig(settingViewModel.config()));
  }

  private void initEvent() {

  }

  private void prepareStart() {
    startModeChoiceBox.setItems(START_MODE_ITEMS);
    startModeChoiceBox.getSelectionModel().select(MODE_NORMAL);
    hoursSpinner.valueFactoryProperty().set(HOURS_VALUE_FACTORY);
    minutesSpinner.valueFactoryProperty().set(MINUTES_VALUE_FACTORY);
    // from mir2-applem2
    startStatus = 0;
    backupStatus = 0;
    disposable.add(JavaFxObservable.valuesOf(startModeChoiceBox.valueProperty())
        .map(s -> s.equals(MODE_NORMAL))
        .subscribe(disabled -> {
          hoursSpinner.setDisable(disabled);
          minutesSpinner.setDisable(disabled);
        }));
    pageTabPane.getSelectionModel().select(controlTab);
    configTabPane.getSelectionModel().select(baseConfigTab);
    consoleTextArea.clear();
    // todo start list memo clear
    consoleMessage("启动已就绪...");
  }

  private void loadConfig() {
    disposable.add(Configs.load().subscribe(this.config::set));
  }

  private void updateLayout() {
    disposable.add(JavaFxObservable.valuesOf(config)
        .doOnNext(this::updateBaseSettingView)
        .doOnNext(this::updateDatabaseSettingView)
        .doOnNext(this::updateAccountSettingView)
        .doOnNext(this::updateLoggerSettingView)
        .doOnNext(this::updateCoreSettingView)
        .doOnNext(this::updateGameSettingView)
        .doOnNext(this::updateRoleSettingView)
        .doOnNext(this::updateLoginSettingView)
        .doOnNext(this::updateRankSettingView)
        // todo update backup view
        .subscribe(this::updateControlView, ThrowableDialog::show));
  }

  private void updateRankSettingView(BootstrapConfig config) {
    rankXSpinner.getValueFactory().setValue(config.rank.x);
    rankYSpinner.getValueFactory().setValue(config.rank.y);
    rankEnabledCheckBox.setSelected(config.rank.enabled);
  }

  private void updateLoginSettingView(BootstrapConfig config) {
    loginXSpinner.getValueFactory().setValue(config.login.x);
    loginYSpinner.getValueFactory().setValue(config.login.y);
    loginPortSpinner.getValueFactory().setValue(config.login.port);
    loginEnabledCheckBox.setSelected(config.login.enabled);
  }

  private void updateRoleSettingView(BootstrapConfig config) {
    roleXSpinner.getValueFactory().setValue(config.role.x);
    roleYSpinner.getValueFactory().setValue(config.role.y);
    rolePortSpinner.getValueFactory().setValue(config.role.port);
    roleEnabledCheckBox.setSelected(config.role.enabled);
  }

  private void updateGameSettingView(BootstrapConfig config) {
    gameXSpinner.getValueFactory().setValue(config.game.x);
    gameYSpinner.getValueFactory().setValue(config.game.y);
    gamePortSpinner.getValueFactory().setValue(config.game.port);
    gameEnabledCheckBox.setSelected(config.game.enabled);
  }

  private void updateCoreSettingView(BootstrapConfig config) {
    coreXSpinner.getValueFactory().setValue(config.core.x);
    coreYSpinner.getValueFactory().setValue(config.core.y);
    corePortSpinner.getValueFactory().setValue(config.core.port);
    coreServerPortSpinner.getValueFactory().setValue(config.core.serverPort);
    coreEnabledCheckBox.setSelected(config.core.enabled);
  }

  private void updateLoggerSettingView(BootstrapConfig config) {
    loggerXSpinner.getValueFactory().setValue(config.logger.x);
    loggerYSpinner.getValueFactory().setValue(config.logger.y);
    loggerPortSpinner.getValueFactory().setValue(config.logger.port);
    loggerEnabledCheckBox.setSelected(config.logger.enabled);
  }

  private void updateAccountSettingView(BootstrapConfig config) {
    accountXSpinner.getValueFactory().setValue(config.account.x);
    accountYSpinner.getValueFactory().setValue(config.account.y);
    accountPortSpinner.getValueFactory().setValue(config.account.port);
    accountServerPortSpinner.getValueFactory().setValue(config.account.serverPort);
    accountPublicPortSpinner.getValueFactory().setValue(config.account.monPort);
    accountEnabledCheckBox.setSelected(config.account.enabled);
  }

  private void updateDatabaseSettingView(BootstrapConfig config) {
    databaseXSpinner.getValueFactory().setValue(config.database.x);
    databaseYSpinner.getValueFactory().setValue(config.database.y);
    databasePortSpinner.getValueFactory().setValue(config.database.port);
    databaseServerPortSpinner.getValueFactory().setValue(config.database.serverPort);
    databaseEnabledCheckBox.setSelected(config.database.enabled);
    //databasePathTextField.setText(config.database.path);
  }

  private void updateBaseSettingView(BootstrapConfig config) {
    homePathTextField.setText(Paths.get(config.homePath).toString());
    databaseNameTextField.setText(config.dbName);
    gameNameTextField.setText(config.gameName);
    gameAddressTextField.setText(config.gameHost);
    backupActionCheckBox.setSelected(config.backupAction);
    combineActionCheckBox.setSelected(config.combineAction);
    wishActionCheckBox.setSelected(config.wishAction);
  }

  private void updateControlView(BootstrapConfig config) {
    databaseCheckBox.setSelected(config.database.enabled);
    accountCheckBox.setSelected(config.account.enabled);
    loggerCheckBox.setSelected(config.logger.enabled);
    coreCheckBox.setSelected(config.core.enabled);
    gameCheckBox.setSelected(config.game.enabled);
    roleCheckBox.setSelected(config.role.enabled);
    loginCheckBox.setSelected(config.login.enabled);
    rankCheckBox.setSelected(config.rank.enabled);
  }

  private void loadBackupList() {

  }

  private void updateBackupLayout() {

  }

  private void consoleMessage(String message) {
    String timestamp = DateTimeHelper.format(Date.from(Instant.now()));
    String console = String.format(Locale.getDefault(), FORMAT_CONSOLE, timestamp, message);
    consoleTextArea.appendText(console);
  }

  private String getHomePath() {
    String home = config.get().homePath;
    if (Strings.isNullOrEmpty(home)) {
      home = System.getProperty("user.dir");
    }
    return home;
  }
}
