package bootstrap.viewmodel;

import com.google.common.base.Strings;
import helper.DateTimeHelper;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.controlsfx.dialog.ExceptionDialog;
import rx.Subscriber;
import bootstrap.model.BootstrapModel;
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
  private static final String REGEX_NUMBER_HUNDRED = "[0-9]+";
  private static final Integer MIN_TIME = 0;
  private static final Integer MAX_DELAY_HOURS = 99;
  private static final Integer MAX_DELAY_MINUTES = 59;
  private static final Integer MAX_TIMING_HOURS = 23;
  private static final Integer MAX_TIMING_MINUTES = 59;

  /*程序控制*/
  @FXML CheckBox databaseCheckBox;
  @FXML CheckBox accountCheckBox;
  @FXML CheckBox loggerCheckBox;
  @FXML CheckBox coreCheckBox;
  @FXML CheckBox gameCheckBox;
  @FXML CheckBox roleCheckBox;
  @FXML CheckBox loginCheckBox;
  @FXML CheckBox rankCheckBox;
  /*启动控制*/
  @FXML RadioButton normalModeRadioButton;
  @FXML RadioButton delayModeRadioButton;
  @FXML RadioButton timingModeRadioButton;
  @FXML ToggleGroup startModeGroup;
  @FXML TextField hoursTextField;
  @FXML TextField minutesTextField;
  /*信息文本*/
  @FXML TextArea consoleTextArea;
  /*启动按钮*/
  @FXML Button startServerButton;

  private final BootstrapModel bootstrapModel = new BootstrapModel();

  @FXML void initialize() {
    Monitor monitor = Monitor.getInstance();
    initState();
    monitor.record("init state");
    initEvent();
    monitor.record("init event");
    bindModel();
    monitor.record("bind model");
    loadConfig();
    monitor.record("load config");
    showConsole("启动已就绪！");
    monitor.report("bootstrap view model initialized");
  }

  @FXML void onStartServerClicked() {
    Monitor monitor = Monitor.getInstance();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    switch (bootstrapModel.state) {
      case DEFAULT:
        alert.setHeaderText("是否启动服务？");
        alert.showAndWait()
            .filter(buttonType -> buttonType == ButtonType.OK)
            .ifPresent(buttonType -> {
              bootstrapModel.startServer(computeStartDateTime(), new Subscriber<Long>() {
                @Override public void onCompleted() {
                  bootstrapModel.state = BootstrapModel.State.RUNNING;
                  startServerButton.setText(bootstrapModel.state.toString());
                  monitor.report("start server completed");
                }

                @Override public void onError(Throwable throwable) {
                  new ExceptionDialog(throwable).show();
                  bootstrapModel.state = BootstrapModel.State.DEFAULT;
                  startServerButton.setText(bootstrapModel.state.toString());
                  monitor.report("start server error");
                }

                @Override public void onNext(Long aLong) {
                  monitor.record("start server: " + aLong);
                }
              });
              bootstrapModel.state = BootstrapModel.State.STARTING;
              startServerButton.setText(bootstrapModel.state.toString());
            });
        break;
      case STARTING:
        alert.setHeaderText("服务正在启动，是否取消？");
        alert.showAndWait()
            .filter(buttonType -> buttonType == ButtonType.OK)
            .ifPresent(buttonType -> {
              bootstrapModel.cancelStart();
              bootstrapModel.state = BootstrapModel.State.DEFAULT;
              startServerButton.setText(bootstrapModel.state.toString());
            });
        break;
      case RUNNING:
        alert.setHeaderText("是否停止服务？");
        alert.showAndWait()
            .filter(buttonType -> buttonType == ButtonType.OK)
            .ifPresent(buttonType -> bootstrapModel.stopGame());
        break;
      case STOPPING:
        alert.setHeaderText("服务正在停止，是否取消？");
        alert.showAndWait()
            .filter(buttonType -> buttonType == ButtonType.OK)
            .ifPresent(buttonType -> bootstrapModel.cancelStop());
        break;
    }
  }

  private void initState() {
    startModeGroup.selectToggle(normalModeRadioButton);
    hoursTextField.setText(MIN_TIME.toString());
    minutesTextField.setText(MIN_TIME.toString());
    hoursTextField.setDisable(true);
    minutesTextField.setDisable(true);
    consoleTextArea.clear();
  }

  private void initEvent() {
    startModeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
        handleStartModeRadioChanged(newValue));
    hoursTextField.textProperty().addListener((observable, oldValue, newValue) ->
        handleHoursTextChanged(newValue, oldValue));
    minutesTextField.textProperty().addListener((observable, oldValue, newValue) ->
        handleMinutesTextChanged(newValue, oldValue));
  }

  private void bindModel() {
    // bindBidirectional 是指双向绑定，意味着数据改动会影响组件状态，同时组件状态改变会更新到数据。
    databaseCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.database.enabled);
    accountCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.account.enabled);
    loggerCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.logger.enabled);
    coreCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.core.enabled);
    gameCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.game.enabled);
    roleCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.role.enabled);
    loginCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.login.enabled);
    rankCheckBox.selectedProperty().bindBidirectional(bootstrapModel.config.rank.enabled);
    // 其他组件不需要双向绑定，比如启动模式单选按钮组，点击切换又不影响配置；
    // 再比如控制台文本区域，只显示每次启动关闭时的信息，不需要保存到配置。
  }

  private void loadConfig() {
    bootstrapModel.loadConfig();
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
}
