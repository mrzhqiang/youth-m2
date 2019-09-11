package youthm2.bootstrap.control;

import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * 启动模式视图模型。
 *
 * @author mrzhqiang
 */
public final class StartModeViewModel {
  private static final String MODE_NORMAL = "正常启动";
  private static final String MODE_DELAY = "延时启动";
  private static final String MODE_TIMING = "定时启动";
  private static final SpinnerValueFactory.IntegerSpinnerValueFactory HOURS_VALUE_FACTORY =
      new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
  private static final SpinnerValueFactory.IntegerSpinnerValueFactory MINUTES_VALUE_FACTORY =
      new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);

  private final StringProperty modeValue = new SimpleStringProperty();
  private final BooleanProperty hoursDisable = new SimpleBooleanProperty(true);
  private final ObjectProperty<SpinnerValueFactory<Integer>> hoursValue =
      new SimpleObjectProperty<>(HOURS_VALUE_FACTORY);
  private final BooleanProperty minutesDisable = new SimpleBooleanProperty(true);
  private final ObjectProperty<SpinnerValueFactory<Integer>> minutesValue =
      new SimpleObjectProperty<>(MINUTES_VALUE_FACTORY);

  public void bind(ChoiceBox<String> startMode, Spinner<Integer> hours, Spinner<Integer> minutes) {
    if (startMode.getItems().isEmpty()) {
      startMode.getItems().addAll(MODE_NORMAL, MODE_DELAY, MODE_TIMING);
    }
    startMode.valueProperty().bindBidirectional(modeValue);
    modeValue.addListener((observable, oldValue, newValue) -> select(newValue));
    startMode.getSelectionModel().select(MODE_NORMAL);
    hours.disableProperty().bindBidirectional(hoursDisable);
    hours.valueFactoryProperty().bindBidirectional(hoursValue);
    minutes.disableProperty().bindBidirectional(minutesDisable);
    minutes.valueFactoryProperty().bindBidirectional(minutesValue);
  }

  public LocalDateTime computeStartDateTime() {
    LocalDateTime now = LocalDateTime.now();
    Integer hours = hoursValue.getValue().getValue();
    Integer minutes = minutesValue.getValue().getValue();
    if (MODE_DELAY.equals(modeValue.get())) {
      return now.plusHours(hours).plusMinutes(minutes);
    }
    if (MODE_TIMING.equals(modeValue.get())) {
      LocalDateTime timing = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hours, minutes));
      // 如果现在已经超过定时，那么给定时加个鸡腿
      return now.isAfter(timing) ? timing.plusDays(1) : timing;
    }
    return now;
  }

  private void select(String selected) {
    if (MODE_NORMAL.equals(selected)) {
      normalMode();
    } else if (MODE_DELAY.equals(selected)) {
      delayMode();
    } else if (MODE_TIMING.equals(selected)) {
      timingMode();
    }
  }

  private void normalMode() {
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
