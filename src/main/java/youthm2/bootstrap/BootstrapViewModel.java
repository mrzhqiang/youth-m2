package youthm2.bootstrap;

import com.google.common.base.Strings;
import com.typesafe.config.ConfigFactory;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.config.BootstrapConfig;
import youthm2.bootstrap.control.ControlViewModel;
import youthm2.bootstrap.setting.SettingViewModel;
import youthm2.common.Monitor;
import youthm2.common.dialog.AlertDialog;
import youthm2.common.dialog.ChooserDialog;
import youthm2.common.util.Networks;
import youthm2.database.DatabaseServer;

/**
 * 引导程序视图模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapViewModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

  /* root view */
  @FXML TabPane pageTabPane;
  @FXML Tab controlTab;
  /* control view */
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
  /* setting view */
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

  private ObjectProperty<BootstrapConfig> config =
      new SimpleObjectProperty<>(BootstrapConfig.of(ConfigFactory.load()));

  private final ControlViewModel controlViewModel = new ControlViewModel();
  private final SettingViewModel settingViewModel = new SettingViewModel();

  private final DatabaseServer databaseServer = new DatabaseServer();
  private final CompositeDisposable disposable = new CompositeDisposable();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    monitor.record("init begin");
    pageTabPane.getSelectionModel().select(controlTab);
    configTabPane.getSelectionModel().select(baseConfigTab);
    monitor.record("layout init");
    bindLayout();
    monitor.record("layout bind");
    initEvent();
    monitor.record("event init");
    controlViewModel.console.append("启动已就绪...");
    monitor.report("initialized");
  }

  private void bindLayout() {
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

    settingViewModel.bind(homePathTextField, databaseNameTextField, gameNameTextField,
        gameAddressTextField, backupActionCheckBox, combineActionCheckBox, wishActionCheckBox);
    settingViewModel.database.bind(databaseXSpinner, databaseYSpinner, databaseEnabledCheckBox,
        databasePathTextField, databasePortSpinner, databaseServerPortSpinner);
    settingViewModel.account.bind(accountXSpinner, accountYSpinner, accountEnabledCheckBox,
        accountPathTextField, accountPortSpinner, accountServerPortSpinner,
        accountPublicPortSpinner);
    settingViewModel.logger.bind(loggerXSpinner, loggerYSpinner, loggerEnabledCheckBox,
        loggerPathTextField, loggerPortSpinner, null);
    settingViewModel.core.bind(coreXSpinner, coreYSpinner, coreEnabledCheckBox, corePathTextField,
        corePortSpinner, coreServerPortSpinner);
    settingViewModel.game.bind(gameXSpinner, gameYSpinner, gameEnabledCheckBox, gamePathTextField,
        gamePortSpinner);
    settingViewModel.role.bind(roleXSpinner, roleYSpinner, roleEnabledCheckBox, rolePathTextField,
        rolePortSpinner);
    settingViewModel.login.bind(loginXSpinner, loginYSpinner, loginEnabledCheckBox,
        loginPathTextField, loginPortSpinner);
    settingViewModel.rank.bind(rankXSpinner, rankYSpinner, rankEnabledCheckBox, rankPathTextField);
  }

  private void initEvent() {
    disposable.addAll(
        JavaFxObservable.valuesOf(config)
            .doOnNext(controlViewModel::update)
            .doOnNext(settingViewModel::update)
            .subscribe(),
        ConfigModel.load()
            .doOnNext(config -> this.config.set(config))
            .subscribe()
    );
  }

  @FXML void onDatabaseServerClicked() {
    if (config.get().database.enabled) {
      AlertDialog.waitConfirm("是否启动数据库服务")
          .ifPresent(buttonType -> {
            controlViewModel.database.starting();
            controlViewModel.console.append("正在启动数据库服务..");
            // todo 需要一个时钟间隔线程，检查启动状态、运行状态、停止状态的情况。
            // 启动时，检查程序是否都启动
            // 运行时，检查程序是否都正常
            // 停止时，检查程序是否都停止
            databaseServer.start(new Stage());
            controlViewModel.database.started();
            controlViewModel.console.append("数据库服务启动完毕..");
          });
    }
  }

  @FXML void onAccountServerClicked() {
  }

  @FXML void onLoggerServerClicked() {
  }

  @FXML void onCoreServerClicked() {
  }

  @FXML void onGameGateClicked() {
  }

  @FXML void onRoleGateClicked() {
  }

  @FXML void onLoginGateClicked() {
  }

  @FXML void onRankPlugClicked() {
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

  private String getHomePath() {
    String home = config.get().homePath;
    if (Strings.isNullOrEmpty(home)) {
      home = System.getProperty("user.dir");
    }
    return home;
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

  private boolean checkConfig() {
    String home = settingViewModel.homePath.getValue().trim();
    File file = new File(home);
    if (Strings.isNullOrEmpty(home) || !file.isDirectory() || !file.exists()) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的服务器目录，请检查");
      alert.show();
      homePathTextField.requestFocus();
      return false;
    }
    if (Strings.isNullOrEmpty(settingViewModel.databaseName.getValue().trim())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的数据库名称，请检查");
      alert.show();
      databaseNameTextField.requestFocus();
      return false;
    }
    if (Strings.isNullOrEmpty(settingViewModel.gameName.getValue().trim())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏名称，请检查");
      alert.show();
      gameNameTextField.requestFocus();
      return false;
    }
    String address = settingViewModel.gameHost.getValue();
    if (Strings.isNullOrEmpty(address) || !Networks.isAddressV4(address)) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏 IP 地址，请检查");
      alert.show();
      gameAddressTextField.requestFocus();
      return false;
    }
    return true;
  }

  public void onDestroy() {
    disposable.dispose();
  }
}
