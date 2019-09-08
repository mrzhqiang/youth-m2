package youthm2.bootstrap.viewmodel;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.rxjavafx.sources.Change;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.model.BackupModel;
import youthm2.bootstrap.model.BootstrapModel;
import youthm2.bootstrap.model.ConfigModel;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.bootstrap.model.program.Program;
import youthm2.common.Monitor;
import youthm2.common.util.Networks;
import youthm2.common.dialog.AlertDialog;
import youthm2.common.dialog.ChooserDialog;
import youthm2.common.dialog.ThrowableDialog;

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
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

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

  private final ObjectProperty<Config> configProperty = new SimpleObjectProperty<>(ConfigFactory.load());
  private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.INITIALIZED);

  private final ControlViewModel controlViewModel = new ControlViewModel();
  private final SettingViewModel settingViewModel = new SettingViewModel();

  private final ConfigModel configModel = new ConfigModel();
  private final BootstrapModel bootstrapModel = new BootstrapModel();
  private final BackupModel backupModel = new BackupModel();

  private final CompositeDisposable disposable = new CompositeDisposable();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    disposable.add(JavaFxObservable.changesOf(configProperty)
        .map(Change::getNewVal)
        .observeOn(JavaFxScheduler.platform())
        .subscribe(controlViewModel::update, ThrowableDialog::show));
    controlViewModel.console.append("启动已就绪...");
    monitor.report("initialized");
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
    Optional<ButtonType> optional = AlertDialog.waitConfirm(message).filter(AlertDialog.isOK());
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
        LOGGER.error("等待启动时出错", throwable);
        controlViewModel.console.append(throwable.getMessage());
        ThrowableDialog.show(throwable);
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
        LOGGER.error("启动时出错", e);
        controlViewModel.console.append(e.getMessage());
        ThrowableDialog.show(e);
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
