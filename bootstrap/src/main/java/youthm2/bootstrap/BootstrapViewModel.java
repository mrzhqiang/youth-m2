package youthm2.bootstrap;

import helper.DateTimeHelper;
import java.sql.Date;
import java.time.Instant;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import youthm2.common.monitor.Monitor;

/**
 * 引导程序的视图模型。
 * <p>
 * 所谓视图模型，就是将视图与模型进行双向绑定，以便任何一方改变时，另外一方都会同时更新。
 *
 * @author qiang.zhang
 */
public final class BootstrapViewModel {
  private static final String FORMAT_CONSOLE = "[%s]: %s\r\n";

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

  private final BackupModel backupModel = new BackupModel();
  private final BootstrapModel bootstrapModel = new BootstrapModel();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    bindModel();
    monitor.record("bind model");
    loadConfig();
    monitor.record("load config");
    showConsole("启动已就绪！");
    monitor.report("bootstrap view model initialized");
  }

  @FXML void onStartServerClicked() {
    showConsole("启动服务");
  }

  private void bindModel() {
    databaseCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.database.enabled);
    accountCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.account.enabled);
    loggerCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.logger.enabled);
    coreCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.core.enabled);
    gameCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.game.enabled);
    roleCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.role.enabled);
    loginCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.login.enabled);
    rankCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.rank.enabled);
    // 其他组件不需要双向绑定，比如启动模式单选按钮组，点击切换又不影响配置
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
}
