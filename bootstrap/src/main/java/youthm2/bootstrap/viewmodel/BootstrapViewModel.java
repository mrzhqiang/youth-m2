package youthm2.bootstrap.viewmodel;

import com.google.common.base.Strings;
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
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import youthm2.bootstrap.model.BackupModel;
import youthm2.bootstrap.model.BootstrapModel;
import youthm2.common.Monitor;
import youthm2.common.dialog.ThrowableDialog;
import youthm2.common.model.AlertModel;
import youthm2.common.model.ChooserModel;
import youthm2.common.model.LoggerModel;
import youthm2.common.model.NetworkModel;

/**
 * 引导程序的视图模型。
 * <p>
 * 所谓视图模型，就是将视图与模型进行双向绑定，以便任何一方改变时，另外一方都会同时更新。
 *
 * @author qiang.zhang
 */
public final class BootstrapViewModel {
  /* 1. 控制面板 */
  @FXML Button databaseServerButton;
  /* 1.1 启动程序*/
  @FXML TabPane pageTabPane;
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
  /* 1.2 启动模式 */
  @FXML ChoiceBox<String> startModeChoiceBox;
  @FXML Spinner<Integer> hoursSpinner;
  @FXML Spinner<Integer> minutesSpinner;
  /* 1.3 启动信息 */
  @FXML TextArea consoleTextArea;
  /* 1.4 一键启动 */
  @FXML Button startGameButton;

  /* 2. 参数配置 */
  @FXML TabPane configTabPane;
  /* 2.1 基本配置 */
  @FXML TextField homePathTextField;
  @FXML TextField databaseNameTextField;
  @FXML TextField gameNameTextField;
  @FXML TextField gameAddressTextField;
  @FXML CheckBox backupActionCheckBox;
  @FXML CheckBox combineActionCheckBox;
  @FXML CheckBox wishActionCheckBox;
  /* 2.2 数据库配置 */
  @FXML Spinner<Integer> databaseXSpinner;
  @FXML Spinner<Integer> databaseYSpinner;
  @FXML Spinner<Integer> databasePortSpinner;
  @FXML Spinner<Integer> databaseServerPortSpinner;
  @FXML CheckBox databaseEnabledCheckBox;
  @FXML TextField databasePathTextField;
  /* 2.3 账号配置 */
  @FXML Spinner<Integer> accountXSpinner;
  @FXML Spinner<Integer> accountYSpinner;
  @FXML Spinner<Integer> accountPortSpinner;
  @FXML Spinner<Integer> accountServerPortSpinner;
  @FXML Spinner<Integer> accountPublicPortSpinner;
  @FXML CheckBox accountEnabledCheckBox;
  @FXML TextField accountPathTextField;
  /* 2.4 日志配置 */
  @FXML Spinner<Integer> loggerXSpinner;
  @FXML Spinner<Integer> loggerYSpinner;
  @FXML Spinner<Integer> loggerPortSpinner;
  @FXML CheckBox loggerEnabledCheckBox;
  @FXML TextField loggerPathTextField;
  /* 2.5 核心配置 */
  @FXML Spinner<Integer> coreXSpinner;
  @FXML Spinner<Integer> coreYSpinner;
  @FXML Spinner<Integer> corePortSpinner;
  @FXML Spinner<Integer> coreServerPortSpinner;
  @FXML CheckBox coreEnabledCheckBox;
  @FXML TextField corePathTextField;
  /* 2.6 游戏配置 */
  @FXML Spinner<Integer> gameXSpinner;
  @FXML Spinner<Integer> gameYSpinner;
  @FXML Spinner<Integer> gamePortSpinner;
  @FXML CheckBox gameEnabledCheckBox;
  @FXML TextField gamePathTextField;
  /* 2.7 角色配置 */
  @FXML Spinner<Integer> roleXSpinner;
  @FXML Spinner<Integer> roleYSpinner;
  @FXML Spinner<Integer> rolePortSpinner;
  @FXML CheckBox roleEnabledCheckBox;
  @FXML TextField rolePathTextField;
  /* 2.8 登陆配置 */
  @FXML Spinner<Integer> loginXSpinner;
  @FXML Spinner<Integer> loginYSpinner;
  @FXML Spinner<Integer> loginPortSpinner;
  @FXML CheckBox loginEnabledCheckBox;
  @FXML TextField loginPathTextField;
  /* 2.9 排行榜配置 */
  @FXML Spinner<Integer> rankXSpinner;
  @FXML Spinner<Integer> rankYSpinner;
  @FXML CheckBox rankEnabledCheckBox;
  @FXML TextField rankPathTextField;

  private final ControlViewModel controlViewModel = new ControlViewModel();
  private final ConfigViewModel configViewModel = new ConfigViewModel();

  private final BootstrapModel bootstrapModel = new BootstrapModel();
  private final BackupModel backupModel = new BackupModel();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    initLayout();
    prepareLayout();
    loadConfig();
    controlViewModel.console.append("启动已就绪...");
    monitor.report("initialized");
  }

  private void initLayout() {
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
        gameAddressTextField, backupActionCheckBox, combineActionCheckBox, wishActionCheckBox);
    configViewModel.database.bind(databaseXSpinner, databaseYSpinner, databaseEnabledCheckBox,
        databasePathTextField, databasePortSpinner, databaseServerPortSpinner);
    configViewModel.account.bind(accountXSpinner, accountYSpinner, accountEnabledCheckBox,
        accountPathTextField, accountPortSpinner, accountServerPortSpinner,
        accountPublicPortSpinner);
    configViewModel.logger.bind(loggerXSpinner, loggerYSpinner, loggerEnabledCheckBox,
        loggerPathTextField, loggerPortSpinner, null);
    configViewModel.core.bind(coreXSpinner, coreYSpinner, coreEnabledCheckBox, corePathTextField,
        corePortSpinner, coreServerPortSpinner);
    configViewModel.game.bind(gameXSpinner, gameYSpinner, gameEnabledCheckBox, gamePathTextField,
        gamePortSpinner);
    configViewModel.role.bind(roleXSpinner, roleYSpinner, roleEnabledCheckBox, rolePathTextField,
        rolePortSpinner);
    configViewModel.login.bind(loginXSpinner, loginYSpinner, loginEnabledCheckBox,
        loginPathTextField, loginPortSpinner);
    configViewModel.rank.bind(rankXSpinner, rankYSpinner, rankEnabledCheckBox, rankPathTextField);
  }

  private void prepareLayout() {
    bootstrapModel.state = BootstrapModel.State.INITIALIZED;
    backupModel.status = 0;
    pageTabPane.getSelectionModel().select(0);
    configTabPane.getSelectionModel().select(0);
    controlViewModel.prepare();
    // todo start list clear from apple m2
  }

  private void loadConfig() {
    bootstrapModel.loadConfig(config -> {
      controlViewModel.update(config);
      configViewModel.update(config);
      configViewModel.database.update(config.database);
      configViewModel.account.update(config.account);
      configViewModel.logger.update(config.logger);
      configViewModel.core.update(config.core);
      configViewModel.game.update(config.game);
      configViewModel.role.update(config.role);
      configViewModel.login.update(config.login);
      configViewModel.rank.update(config.rank);
      controlViewModel.console.append("配置加载完毕.");
    });
    backupModel.loadConfig(dataList -> {
      // TODO: 2019/7/23 显示备份数据到表格中
    });
    // TODO: 2019/7/23 判断是否启动备份系统，是就去启动它
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
    Optional<ButtonType> optional =
        AlertModel.waitConfirm(bootstrapModel.state.getMessage())
            .filter(AlertModel.isOK());
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

  private void startServer() {
    LocalDateTime targetTime = controlViewModel.startMode.computeStartDateTime();
    bootstrapModel.startServer(targetTime, new BootstrapModel.OnStartServerListener() {
      @Override public void onStart() {
        bootstrapModel.state = BootstrapModel.State.STARTING;
        controlViewModel.startGame.starting();
      }

      @Override public void onError(Throwable throwable) {
        LoggerModel.BOOTSTRAP.error("一键启动出错", throwable);
        ThrowableDialog.show(throwable);
        controlViewModel.console.append(throwable.getMessage());
        bootstrapModel.state = BootstrapModel.State.INITIALIZED;
        controlViewModel.startGame.stopped();
      }

      @Override public void onCompleted() {
        controlViewModel.console.append("一键启动完毕。");
        bootstrapModel.state = BootstrapModel.State.RUNNING;
        controlViewModel.startGame.running();
        // todo check all server running status
      }
    });
  }

  @FXML void onFoundHomePathClicked() {
    ChooserModel.directory("请选择服务端目录", new File(getHomePath()))
        .ifPresent(file -> homePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundDatabaseFileClicked() {
    ChooserModel.file("请选择要启动的数据库服务", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> databasePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundAccountFileClicked() {
    ChooserModel.file("请选择要启动的账号服务", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> accountPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundLoggerFileClicked() {
    ChooserModel.file("请选择要启动的日志服务", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> loggerPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundCoreFileClicked() {
    ChooserModel.file("请选择要启动的核心服务", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> corePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundGameFileClicked() {
    ChooserModel.file("请选择要启动的游戏网关", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> gamePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundRoleFileClicked() {
    ChooserModel.file("请选择要启动的角色网关", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> rolePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundLoginFileClicked() {
    ChooserModel.file("请选择要启动的登陆网关", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> loginPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundRankFileClicked() {
    ChooserModel.file("请选择要启动的排行榜插件", new File(getHomePath()), ChooserModel.exeFilter())
        .ifPresent(file -> rankPathTextField.setText(file.getPath()));
  }

  private String getHomePath() {
    String home = configViewModel.homePath.getValue();
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
        AlertModel.waitConfirm("恢复默认基本配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(configViewModel::update));
        break;
      case 1:
        AlertModel.waitConfirm("恢复默认数据库配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.database.update(config.database)));
        break;
      case 2:
        AlertModel.waitConfirm("恢复默认账号配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.account.update(config.account)));
        break;
      case 3:
        AlertModel.waitConfirm("恢复默认日志配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.logger.update(config.logger)));
        break;
      case 4:
        AlertModel.waitConfirm("恢复默认核心配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.core.update(config.core)));
        break;
      case 5:
        AlertModel.waitConfirm("恢复默认游戏配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.game.update(config.game)));
        break;
      case 6:
        AlertModel.waitConfirm("恢复默认角色配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.role.update(config.role)));
        break;
      case 7:
        AlertModel.waitConfirm("恢复默认登陆配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.login.update(config.login)));
        break;
      case 8:
        AlertModel.waitConfirm("恢复默认排行榜配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadDefaultConfig(
                config -> configViewModel.rank.update(config.rank)));
        break;
    }
  }

  @FXML void onConfigReloadClicked() {
    SingleSelectionModel<Tab> selectionModel = configTabPane.getSelectionModel();
    switch (selectionModel.getSelectedIndex()) {
      default:
      case 0:
        AlertModel.waitConfirm("重新加载基本配置？", "此操作将恢复当前配置为最近一次数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(configViewModel::update));
        break;
      case 1:
        AlertModel.waitConfirm("重新加载数据库配置？", "此操作将恢复当前配置为最近一次数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.database.update(config.database)));
        break;
      case 2:
        AlertModel.waitConfirm("重新加载账号配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.account.update(config.account)));
        break;
      case 3:
        AlertModel.waitConfirm("重新加载日志配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.logger.update(config.logger)));
        break;
      case 4:
        AlertModel.waitConfirm("重新加载核心配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.core.update(config.core)));
        break;
      case 5:
        AlertModel.waitConfirm("重新加载游戏配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.game.update(config.game)));
        break;
      case 6:
        AlertModel.waitConfirm("重新加载角色配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.role.update(config.role)));
        break;
      case 7:
        AlertModel.waitConfirm("重新加载登陆配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.login.update(config.login)));
        break;
      case 8:
        AlertModel.waitConfirm("重新加载排行榜配置？", "此操作将恢复当前配置为内置默认数值。")
            .filter(AlertModel.isOK())
            .ifPresent(type -> bootstrapModel.loadConfig(
                config -> configViewModel.rank.update(config.rank)));
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
    AlertModel.waitConfirm("保存当前所有配置？", "此操作将重新生成配置文件。")
        .filter(AlertModel.isOK())
        .map(type -> checkConfig())
        .ifPresent(type -> bootstrapModel.saveConfig(configViewModel.config()));
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

  private boolean checkConfig() {
    String home = configViewModel.homePath.getValue().trim();
    File file = new File(home);
    if (Strings.isNullOrEmpty(home) || !file.isDirectory() || !file.exists()) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的服务器目录，请检查");
      alert.show();
      homePathTextField.requestFocus();
      return false;
    }
    if (Strings.isNullOrEmpty(configViewModel.databaseName.getValue().trim())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的数据库名称，请检查");
      alert.show();
      databaseNameTextField.requestFocus();
      return false;
    }
    if (Strings.isNullOrEmpty(configViewModel.gameName.getValue().trim())) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏名称，请检查");
      alert.show();
      gameNameTextField.requestFocus();
      return false;
    }
    String address = configViewModel.gameHost.getValue();
    if (Strings.isNullOrEmpty(address) || !NetworkModel.isAddressV4(address)) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏 IP 地址，请检查");
      alert.show();
      gameAddressTextField.requestFocus();
      return false;
    }
    return true;
  }
}
