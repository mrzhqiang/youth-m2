package youthm2.bootstrap.viewmodel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import helper.DateTimeHelper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * 控制面板视图模型。
 *
 * @author qiang.zhang
 */
final class ControlViewModel {
  final StartProgramViewModel database = new StartProgramViewModel();
  final StartProgramViewModel account = new StartProgramViewModel();
  final StartProgramViewModel logger = new StartProgramViewModel();
  final StartProgramViewModel core = new StartProgramViewModel();
  final StartProgramViewModel game = new StartProgramViewModel();
  final StartProgramViewModel role = new StartProgramViewModel();
  final StartProgramViewModel login = new StartProgramViewModel();
  final StartProgramViewModel rank = new StartProgramViewModel();

  final StartModeViewModel startMode = new StartModeViewModel();
  final ConsoleViewModel console = new ConsoleViewModel();
  final StartGameViewModel startGame = new StartGameViewModel();

  void init() {
    database.stopped();
    account.stopped();
    logger.stopped();
    core.stopped();
    game.stopped();
    role.stopped();
    login.stopped();
    rank.stopped();
    startMode.normalMode();
  }

  void update(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    database.disabled(config.getConfig("database"));
    account.disabled(config.getConfig("account"));
    logger.disabled(config.getConfig("logger"));
    core.disabled(config.getConfig("core"));
    game.disabled(config.getConfig("game"));
    role.disabled(config.getConfig("role"));
    login.disabled(config.getConfig("login"));
    rank.disabled(config.getConfig("rank"));
  }

  /**
   * 启动程序视图模型。
   */
  static final class StartProgramViewModel {
    private static final String TEXT_DISABLED = "禁用";
    private static final Color COLOR_DISABLED = Color.valueOf("#D32F2F");
    private static final String TEXT_STOPPED = "未启动";
    private static final Color COLOR_STOPPED = Color.valueOf("#388E3C");
    private static final String TEXT_STARTING = "正在启动";
    private static final Color COLOR_STARTING = Color.valueOf("#FFA000");
    private static final String TEXT_STARTED = "已启动";
    private static final Color COLOR_STARTED = Color.valueOf("#D32F2F");
    private static final String TEXT_STOPPING = "正在停止";
    private static final Color COLOR_STOPPING = Color.valueOf("#9E9E9E");

    final BooleanProperty disable = new SimpleBooleanProperty();
    final StringProperty text = new SimpleStringProperty();
    final Property<Paint> color = new SimpleObjectProperty<>();

    void bind(Button button, Label label) {
      // bindBidirectional 是指双向绑定，意味着数据改动会影响组件状态，同时组件状态改变会更新到数据。
      // 需要注意的是：绑定从一开始就将目标对象的值设置给调用者；解绑则只是移除了监听器。
      button.disableProperty().bindBidirectional(disable);
      label.textProperty().bindBidirectional(text);
      label.textFillProperty().bindBidirectional(color);
    }

    void disabled(Config config) {
      if (config == null || !config.hasPath("enabled") || !config.getBoolean("enabled")) {
        disable.setValue(true);
        text.setValue(TEXT_DISABLED);
        color.setValue(COLOR_DISABLED);
      }
    }

    void starting() {
      disable.setValue(true);
      text.setValue(TEXT_STARTING);
      color.setValue(COLOR_STARTING);
    }

    void started() {
      disable.setValue(false);
      text.setValue(TEXT_STARTED);
      color.setValue(COLOR_STARTED);
    }

    void stopping() {
      disable.setValue(true);
      text.setValue(TEXT_STOPPING);
      color.setValue(COLOR_STOPPING);
    }

    void stopped() {
      disable.setValue(false);
      text.setValue(TEXT_STOPPED);
      color.setValue(COLOR_STOPPED);
    }
  }

  /**
   * 启动模式视图模型。
   */
  static final class StartModeViewModel {
    private static final String MODE_NORMAL = "正常启动";
    private static final String MODE_DELAY = "延时启动";
    private static final String MODE_TIMING = "定时启动";
    private static final IntegerSpinnerValueFactory HOURS_VALUE_FACTORY =
        new IntegerSpinnerValueFactory(0, 23, 0);
    private static final IntegerSpinnerValueFactory MINUTES_VALUE_FACTORY =
        new IntegerSpinnerValueFactory(0, 59, 0);

    final StringProperty modeValue = new SimpleStringProperty();
    final BooleanProperty hoursDisable = new SimpleBooleanProperty();
    final ObjectProperty<SpinnerValueFactory<Integer>> hoursValue =
        new SimpleObjectProperty<>(HOURS_VALUE_FACTORY);
    final BooleanProperty minutesDisable = new SimpleBooleanProperty();
    final ObjectProperty<SpinnerValueFactory<Integer>> minutesValue =
        new SimpleObjectProperty<>(MINUTES_VALUE_FACTORY);

    void bind(ChoiceBox<String> startMode, Spinner<Integer> hours, Spinner<Integer> minutes) {
      if (startMode.getItems().isEmpty()) {
        startMode.getItems().addAll(MODE_NORMAL, MODE_DELAY, MODE_TIMING);
      }
      startMode.valueProperty().bindBidirectional(modeValue);
      startMode.getSelectionModel().select(MODE_NORMAL);
      hours.disableProperty().bindBidirectional(hoursDisable);
      hours.valueFactoryProperty().bindBidirectional(hoursValue);
      minutes.disableProperty().bindBidirectional(minutesDisable);
      minutes.valueFactoryProperty().bindBidirectional(minutesValue);
    }

    void select(String selected) {
      if (MODE_NORMAL.equals(selected)) {
        normalMode();
      } else if (MODE_DELAY.equals(selected)) {
        delayMode();
      } else if (MODE_TIMING.equals(selected)) {
        timingMode();
      }
    }

    LocalDateTime computeStartDateTime() {
      Integer hours = hoursValue.getValue().getValue();
      Integer minutes = minutesValue.getValue().getValue();
      if (MODE_DELAY.equals(modeValue.get())) {
        return LocalDateTime.now().plusHours(hours).plusMinutes(minutes);
      } else if (MODE_TIMING.equals(modeValue.get())) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timing = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hours, minutes));
        // 如果现在已经超过定时，那么给定时加个鸡腿（加一天）
        if (now.isAfter(timing)) {
          timing = timing.plusDays(1);
        }
        return timing;
      }
      return LocalDateTime.now();
    }

    void normalMode() {
      hoursDisable.setValue(true);
      minutesDisable.setValue(true);
    }

    private void delayMode() {
      hoursDisable.setValue(false);
      minutesDisable.setValue(false);
    }

    private void timingMode() {
      hoursDisable.setValue(false);
      minutesDisable.setValue(false);
    }
  }

  /**
   * 控制台视图模型。
   *
   * @author qiang.zhang
   */
  static final class ConsoleViewModel {
    private static final String FORMAT_CONSOLE = "[%s]: %s\r\n";

    private final StringProperty newMessage = new SimpleStringProperty();
    private final StringProperty text = new SimpleStringProperty();
    private ChangeListener<String> changeListener;

    void bind(TextArea textArea) {
      if (changeListener == null) {
        changeListener = (observable, oldValue, newValue) -> textArea.appendText(newValue);
        newMessage.addListener(changeListener);
      }
      textArea.textProperty().bindBidirectional(text);
    }

    void clean() {
      text.setValue("");
    }

    void append(String message) {
      if (Strings.isNullOrEmpty(message)) {
        return;
      }
      String timestamp = DateTimeHelper.format(Date.from(Instant.now()));
      String console = String.format(Locale.getDefault(), FORMAT_CONSOLE, timestamp, message);
      newMessage.setValue(console);
    }
  }

  /**
   * 启动游戏按钮视图模型。
   *
   * @author qiang.zhang
   */
  static final class StartGameViewModel {
    private static final String LABEL_STOPPED = "一键启动";
    private static final String LABEL_STARTING = "正在启动";
    private static final String LABEL_RUNNING = "正在运行";
    private static final String LABEL_STOPPING = "正在停止";

    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty disable = new SimpleBooleanProperty();

    void bind(Button button) {
      button.textProperty().bindBidirectional(text);
      button.disableProperty().bindBidirectional(disable);
    }

    void stopped() {
      text.setValue(LABEL_STOPPED);
      disable.setValue(false);
    }

    void starting() {
      text.setValue(LABEL_STARTING);
      disable.setValue(true);
    }

    void running() {
      text.setValue(LABEL_RUNNING);
      disable.setValue(false);
    }

    void stopping() {
      text.setValue(LABEL_STOPPING);
      disable.setValue(true);
    }
  }
}
