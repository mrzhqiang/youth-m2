package youthm2.bootstrap.viewmodel;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import youthm2.bootstrap.model.ConfigModel;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.bootstrap.model.program.Program;
import youthm2.common.Monitor;
import youthm2.common.model.LoggerModel;
import youthm2.common.model.NetworkModel;
import youthm2.common.viewmodel.AlertViewModel;
import youthm2.common.viewmodel.ChooserViewModel;
import youthm2.common.viewmodel.ThrowableDialogViewModel;

/**
 * 引导程序的视图模型。
 * <p>
 * 所谓视图模型，就是将视图与模型进行双向绑定，以便任何一方改变时，另外一方都会同时更新。
 * <p>
 * 因此，先做一个小约定，视图模型中只存在对 视图 和 模型 的调用，通常界面相关的改动都通过双向绑定发出通知。
 * <p>
 * 比如：
 * <pre>
 * 用户与界面交互：user >>> view >>> view model >>> object property
 * 模型与界面交互：model >>> object property >>> view model >>> view
 * </pre>
 * <p>
 * MVVM 架构模式最主要的就是 view model，它相当于 MVC 中的 C，MVP 中的 P，但比它们更容易拆分。
 * <p>
 * 拆分的目的是为了面向对象，将界面逐渐解耦成一个个视图模型。
 *
 * @author qiang.zhang
 */
public final class BootstrapViewModel {
  /* 1. 控制面板 */
  @FXML Button databaseServerButton;
  /* 1.1 启动程序 */
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
  /* 3. 备份管理 */
  /* 4. 数据清理 */

  private final ObjectProperty<Config> config = new SimpleObjectProperty<>(ConfigFactory.load());
  private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.INITIALIZED);

  private final ControlViewModel controlViewModel = new ControlViewModel();
  private final SettingViewModel settingViewModel = new SettingViewModel();

  private final ConfigModel configModel = new ConfigModel();
  private final BootstrapModel bootstrapModel = new BootstrapModel();
  private final BackupModel backupModel = new BackupModel();


  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    bindLayout();
    addEvent();
    prepareStart();
    loadConfig();
    controlViewModel.console.append("启动已就绪...");
    monitor.report("initialized");
  }

  private void bindLayout() {
    /* 控制面板 */
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
    /* 参数配置 */
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

  private void addEvent() {
    state.addListener((observable, oldValue, newValue) -> {
      switch (newValue) {
        case INITIALIZED:
          controlViewModel.update(settingViewModel.config.get());
          controlViewModel.startGame.stopped();
          break;
        case WAITING:
          controlViewModel.startGame.waiting();
          break;
        case STARTING:
          controlViewModel.startGame.starting();
          break;
        case RUNNING:
          controlViewModel.startGame.running();
          break;
        case STOPPING:
          controlViewModel.startGame.stopping();
          break;
      }
    });
  }

  private void prepareStart() {
    state.setValue(State.INITIALIZED);
    backupModel.status = 0;
    pageTabPane.getSelectionModel().select(0);
    configTabPane.getSelectionModel().select(0);
    // todo start list clear from apple m2
  }

  private void loadConfig() {
    configModel.load(newValue -> {
      settingViewModel.config.setValue(newValue);
      controlViewModel.console.append("配置加载成功.");
    });
    backupModel.loadConfig(dataList -> {
      // TODO: 2019/7/23 显示备份数据到表格中
      // TODO: 2019/7/23 判断是否启动备份系统，是就去启动它
    });
  }

  @FXML void onDatabaseServerClicked() {

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
    State state = this.state.get();
    String message = state.getMessage();
    Optional<ButtonType> optional = AlertViewModel.waitConfirm(message).filter(AlertViewModel.isOK());
    switch (state) {
      case INITIALIZED:
        optional.ifPresent(buttonType -> attemptStart());
        break;
      case WAITING:
        optional.ifPresent(type -> cancelWaiting());
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

  private void attemptStart() {
    state.setValue(State.WAITING);
    controlViewModel.console.append("准备一键启动..");
    LocalDateTime time = controlViewModel.startMode.computeStartDateTime();
    bootstrapModel.waiting(time, new BootstrapModel.OnWaitingListener() {
      @Override public void onError(Throwable throwable) {
        state.setValue(State.INITIALIZED);
        LoggerModel.BOOTSTRAP.error("等待启动时出错", throwable);
        controlViewModel.console.append(throwable.getMessage());
        ThrowableDialogViewModel.show("等待启动时出错", throwable);
      }

      @Override public void onFinish() {
        BootstrapConfig config = settingViewModel.config.get();
        startServer(config);
      }
    });
  }

  private void startServer(BootstrapConfig config) {
    state.setValue(State.STARTING);
    bootstrapModel.start(config, new BootstrapModel.OnStartListener() {
      @Override public void onError(Throwable e) {
        state.setValue(State.INITIALIZED);
        LoggerModel.BOOTSTRAP.error("启动时出错", e);
        controlViewModel.console.append(e.getMessage());
        ThrowableDialogViewModel.show("启动时出错", e);
      }

      @Override public void onStart(Program program) {
        //controlViewModel.console.append(message);
      }

      @Override public void onFinish() {
        state.setValue(State.RUNNING);
        controlViewModel.console.append("启动完毕");
      }
    });
  }

  private void cancelWaiting() {
    state.setValue(State.INITIALIZED);
    bootstrapModel.stopWaiting();
    controlViewModel.console.append("已取消启动。");
  }

  @FXML void onFoundHomePathClicked() {
    ChooserViewModel.directory("请选择服务端目录", new File(getHomePath()))
        .ifPresent(file -> homePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundDatabaseFileClicked() {
    ChooserViewModel.file("请选择要启动的数据库服务", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> databasePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundAccountFileClicked() {
    ChooserViewModel.file("请选择要启动的账号服务", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> accountPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundLoggerFileClicked() {
    ChooserViewModel.file("请选择要启动的日志服务", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> loggerPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundCoreFileClicked() {
    ChooserViewModel.file("请选择要启动的核心服务", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> corePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundGameFileClicked() {
    ChooserViewModel.file("请选择要启动的游戏网关", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> gamePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundRoleFileClicked() {
    ChooserViewModel.file("请选择要启动的角色网关", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> rolePathTextField.setText(file.getPath()));
  }

  @FXML void onFoundLoginFileClicked() {
    ChooserViewModel.file("请选择要启动的登陆网关", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> loginPathTextField.setText(file.getPath()));
  }

  @FXML void onFoundRankFileClicked() {
    ChooserViewModel.file("请选择要启动的排行榜插件", new File(getHomePath()), ChooserViewModel.exeFilter())
        .ifPresent(file -> rankPathTextField.setText(file.getPath()));
  }

  private String getHomePath() {
    String home = settingViewModel.homePath.getValue();
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

  private void cancelStopServer() {
    bootstrapModel.cancelStop();
  }

  private void stopServer() {
    bootstrapModel.stop();
  }

  private void cancelStartServer() {
    bootstrapModel.cancelStart();
    //bootstrapModel.state = BootstrapModel.State.INITIALIZED;
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
    if (Strings.isNullOrEmpty(address) || !NetworkModel.isAddressV4(address)) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("无效的游戏 IP 地址，请检查");
      alert.show();
      gameAddressTextField.requestFocus();
      return false;
    }
    return true;
  }

  public enum State {
    INITIALIZED("启动服务", "是否启动服务？"),
    WAITING("停止等待", "是否停止等待？"),
    STARTING("正在启动..", "正在启动，是否停止？"),
    RUNNING("停止服务", "是否停止服务？"),
    STOPPING("正在停止..", "正在停止，是否取消？"),
    ;

    private final String label;
    private final String message;

    State(String label, String message) {
      this.label = label;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
